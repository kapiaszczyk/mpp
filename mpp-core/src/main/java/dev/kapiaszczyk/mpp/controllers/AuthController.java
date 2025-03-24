package dev.kapiaszczyk.mpp.controllers;

import dev.kapiaszczyk.mpp.constants.Urls;
import dev.kapiaszczyk.mpp.endpoints.AuthEndpoints;
import dev.kapiaszczyk.mpp.errors.OperationError;
import dev.kapiaszczyk.mpp.models.database.User;
import dev.kapiaszczyk.mpp.requests.authentication.AuthenticationRequest;
import dev.kapiaszczyk.mpp.requests.authentication.RefreshTokenRequest;
import dev.kapiaszczyk.mpp.requests.authentication.RegistrationRequest;
import dev.kapiaszczyk.mpp.services.AlbumService;
import dev.kapiaszczyk.mpp.services.UserService;
import dev.kapiaszczyk.mpp.tokens.JWTService;
import dev.kapiaszczyk.mpp.util.Either;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static dev.kapiaszczyk.mpp.constants.Constants.*;

/**
 * Controller class for handling authentication models
 * including login, refresh token and logout operations
 */
@RestController
@RequestMapping(Urls.AUTH_URL)
@PropertySource("classpath:application.properties")
public class AuthController implements AuthEndpoints {

    @Value("${mpp-core.api-key}")
    private String apiKey;

    @Autowired
    private final AuthenticationManager authenticationManager;

    @Autowired
    private final JWTService tokenService;

    @Autowired
    private final UserService userService;

    @Autowired
    private final AlbumService albumService;

    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isEmpty()) {
            logger.error("API key for internal services was not set - internal service token endpoint will not work");
        } else {
            logger.info("API key for internal services was set");
        }
    }

    public AuthController(AuthenticationManager authenticationManager, JWTService tokenService, UserService userService, AlbumService albumService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.userService = userService;
        this.albumService = albumService;
    }

    @PostMapping(Urls.LOGIN_URL)
    public ResponseEntity<?> login(@Valid @RequestBody AuthenticationRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
            Map<String, String> tokens = tokenService.generateTokensForAuthenticatedUser(authentication.getName(), (Collection<GrantedAuthority>) authentication.getAuthorities());
            return ResponseEntity.ok(tokens);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping(Urls.REFRESH_TOKEN_URL)
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshRequest) {
        Optional<Map<String, String>> tokens = tokenService.generateFreshTokens(refreshRequest.getRefreshToken());
        return tokens.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping(Urls.LOGOUT_URL)
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get(REFRESH_TOKEN_HEADER);
        String accessToken = request.get(ACCESS_TOKEN_HEADER);

        if (refreshToken == null || accessToken == null) {
            return ResponseEntity.badRequest().body("Both refreshToken and accessToken are required");
        }

        tokenService.discardTokens(accessToken, refreshToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping(Urls.INTERNAL_SERVICE_TOKEN_URL)
    public ResponseEntity<?> issueToken(@RequestHeader("Authorization") String authHeader) {
        if (!(apiKey).equals(authHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid API key");
        }
        String token = tokenService.generateTokenForService(TAG_SERVICE_NAME);
        return ResponseEntity.ok(Map.of("token", token));
    }

    // TODO: The validation reveals a lot of information about the internals of the application
    // The exception of the validation should be handled and a generic error message should be returned
    @PostMapping(Urls.REGISTER_URL)
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationRequest registrationRequest) {
        Either<OperationError, User> result = userService.createUser(registrationRequest.getEmail(), registrationRequest.getUsername(), registrationRequest.getPassword());
        if (result.isLeft()) {
            return ResponseEntity.status(result.left().get().getHttpStatus()).body(result.left().get().getMessage());
        } else {
            albumService.createRootAlbumForUser(result.right().get());
            return ResponseEntity.status(HttpStatus.CREATED).body("User created");
        }
    }
}
