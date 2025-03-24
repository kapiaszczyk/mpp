package dev.kapiaszczyk.mpp.requests.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents request to register a new user.
 */
public class RegistrationRequest {

    @NotBlank
    String username;

    @NotBlank
    @Email
    String email;

    @NotBlank
    String password;

    public RegistrationRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
