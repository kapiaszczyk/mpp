package dev.kapiaszczyk.mpp.tokens;

import dev.kapiaszczyk.mpp.constants.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for generating and validating JWT tokens.
 */
@Component
public class JWTUtil {

    private static final long ACCESS_TOKEN_EXPIRATION = Constants.ACCESS_TOKEN_EXPIRATION;
    private static final long REFRESH_TOKEN_EXPIRATION = Constants.REFRESH_TOKEN_EXPIRATION;

    private static final String ISSUER = Constants.ISSUER;
    private static final String AUDIENCE = Constants.AUDIENCE;
    private static final String ROLE_CLAIM = Constants.ROLE_CLAIM;

    @Autowired
    private JwtEncoder encoder;

    @Autowired
    private JwtDecoder decoder;

    /**
     * Generate an access token for the given email and roles.
     *
     * @param email email
     * @param roles roles
     * @return access token
     */
    protected String generateAccessToken(String email, Collection<GrantedAuthority> roles) {

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .audience(List.of(AUDIENCE))
                .issuedAt(now)
                .expiresAt(calculateExpiration(ACCESS_TOKEN_EXPIRATION, now))
                .subject(email)
                .claim(ROLE_CLAIM, roles.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .build();

        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /**
     * Generate a refresh token for the given email.
     *
     * @param email email
     * @return refresh token
     */
    protected String generateRefreshToken(String email) {
        Instant now = Instant.now();

        // Refresh token is not issued with roles
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .audience(List.of(AUDIENCE))
                .issuedAt(now)
                .expiresAt(calculateExpiration(REFRESH_TOKEN_EXPIRATION, now))
                .subject(email)
                .build();

        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    protected String extractEmailFromToken(String token) {
        return this.decoder.decode(token).getSubject();
    }

    protected Instant getExpirationDate(String token) {
        return this.decoder.decode(token).getExpiresAt();
    }

    protected boolean isTokenExpired(String token) {
        try {
            return this.decoder.decode(token).getExpiresAt().isBefore(Instant.now());
        } catch (JwtException e) {
            return true;
        }
    }

    /**
     * Generate a token for external services (e.g. tagging service) which is longer-lived.
     *
     * @param serviceName service name
     * @return token
     */
    protected String generateTokenForService(String serviceName) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .audience(List.of(AUDIENCE))
                .issuedAt(now)
                .expiresAt(calculateExpiration(Constants.SERVICE_TOKEN_EXPIRATION, now))
                .claim(ROLE_CLAIM, List.of("INTERNAL_SERVICE"))
                .subject(serviceName)
                .build();

        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    private Instant calculateExpiration(long expiration, Instant now) {
        return now.plusSeconds(expiration);
    }

}
