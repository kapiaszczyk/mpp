package dev.kapiaszczyk.mpp.endpoints;

import dev.kapiaszczyk.mpp.requests.authentication.AuthenticationRequest;
import dev.kapiaszczyk.mpp.requests.authentication.RefreshTokenRequest;
import dev.kapiaszczyk.mpp.requests.authentication.RegistrationRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Endpoint for authentication operations.
 */
@Tag(
        name = "Authentication Endpoints",
        description = "Operations related to user authentication and authorization."
)
public interface AuthEndpoints {

    /**
     * Handles login request by authenticating user and generating tokens.
     *
     * @param authRequest request containing email and password
     * @return response containing access and refresh tokens
     */
    ResponseEntity<?> login(AuthenticationRequest authRequest);

    /**
     * Handles refresh token request by generating new access and refresh tokens.
     *
     * @param refreshRequest request containing refresh token
     * @return response containing new access and refresh tokens
     */
    ResponseEntity<?> refreshToken(RefreshTokenRequest refreshRequest);

    /**
     * Handles logout request by adding refresh token to blacklist.
     *
     * @param request tokens to be blacklisted
     * @return response with status 200
     */
    ResponseEntity<?> logout(Map<String, String> request);

    /**
     * Issue a token for an internal service.
     *
     * @param authHeader header containing the API key
     * @return response containing the access token
     */
    ResponseEntity<?> issueToken(String authHeader);


    /**
     * Handles registration request by creating new user.
     *
     * @param registrationRequest request containing user data
     * @return response with status 200
     */
    ResponseEntity<?> register(RegistrationRequest registrationRequest);

}
