package dev.kapiaszczyk.mpp.requests.authentication;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents request to refresh an access token.
 */
public class RefreshTokenRequest {
    @NotBlank
    private final String refreshToken;

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

}
