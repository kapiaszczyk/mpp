package dev.kapiaszczyk.mpp.tokens;

import dev.kapiaszczyk.mpp.constants.Constants;
import dev.kapiaszczyk.mpp.models.database.User;
import dev.kapiaszczyk.mpp.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service for handling operations on JWT tokens
 * including generation, validation and blacklisting.
 */
@Service
@Scope("singleton")
public class JWTService {

    private static final Logger logger = LoggerFactory.getLogger(JWTService.class);
    private final JWTUtil jwtUtil;
    private final JWTBlacklist jwtBlacklist;
    private final UserRepository userRepository;
    public JWTService(JWTUtil jwtUtil, JWTBlacklist jwtBlacklist, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.jwtBlacklist = jwtBlacklist;
        this.userRepository = userRepository;
    }

    // TODO: This is a bad practice, as it allows for token generation without authentication when used in the wrong context.
    /**
     * Generates initial access and refresh tokens for a user
     * based on their email and roles. Should not be used if user is not authenticated.
     *
     * @param email user's email
     * @param roles user's roles
     * @return map containing access and refresh tokens
     */
    public Map<String, String> generateTokensForAuthenticatedUser(String email, Collection<GrantedAuthority> roles) {
        String accessToken = jwtUtil.generateAccessToken(email, roles);
        String refreshToken = jwtUtil.generateRefreshToken(email);

        Map<String, String> tokens = new HashMap<>();
        tokens.put(Constants.ACCESS_TOKEN_HEADER, accessToken);
        tokens.put(Constants.REFRESH_TOKEN_HEADER, refreshToken);

        logger.info("Generated tokens for user with email: {}", email);

        return tokens;
    }

    /**
     * Generates new access and refresh tokens for a user
     * based on their email and roles after providing a valid refresh token.
     *
     * @param refreshToken existing refresh token
     * @return map containing new access and refresh tokens
     */
    public Optional<Map<String, String>> generateFreshTokens(String refreshToken) {
        if (isTokenValid(refreshToken)) {
            logger.warn("Refresh token is invalid or expired");
            return Optional.empty();
        }

        String email = jwtUtil.extractEmailFromToken(refreshToken);
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            logger.warn("User with email: {} not found", email);
            return Optional.empty();
        } else {
            logger.info("Generated fresh tokens for user with email: {}", email);
            addToBlacklist(refreshToken);
            logger.info("Refresh token added to blacklist");
            return Optional.of(generateTokensForAuthenticatedUser(user.get().getEmail(), (Collection<GrantedAuthority>) user.get().getAuthorities()));
        }
    }

    /**
     * Generate a token for external services (e.g. tagging service)
     *
     * @param serviceName name of the service
     * @return token
     */
    public String generateTokenForService(String serviceName) {
        logger.info("Generated token for service: {}", serviceName);
        return jwtUtil.generateTokenForService(serviceName);
    }

    /**
     * Discard access and refresh tokens by adding them to the blacklist.
     *
     * @param accessToken  access token
     * @param refreshToken refresh token
     */
    public void discardTokens(String accessToken, String refreshToken) {
        addToBlacklist(accessToken);
        addToBlacklist(refreshToken);
    }

    public boolean isTokenValid(String token) {
        return jwtUtil.isTokenExpired(token) || jwtBlacklist.isTokenBlacklisted(token);
    }

    private void addToBlacklist(String token) {
        jwtBlacklist.addToBlacklist(token, jwtUtil.getExpirationDate(token).getEpochSecond());
    }

}
