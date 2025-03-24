package dev.kapiaszczyk.mpp.requests.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents request to authenticate a user.
 */
public class AuthenticationRequest {

    @NotBlank
    @Email
    String email;

    @NotBlank
    String password;

    public AuthenticationRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

}
