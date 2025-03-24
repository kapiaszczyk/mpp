package dev.kapiaszczyk.mpp.models;

import java.util.List;

/**
 * Represents the roles in the system.
 */
public enum SystemRoles {

    USER("USER"),
    ADMIN("ADMIN");

    private final String authority;

    SystemRoles(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }

    public static boolean isRoleValid(String role) {
        return role.equals(USER.getAuthority()) || role.equals(ADMIN.getAuthority());
    }

    public static List<String> getAllRoles() {
        return List.of(USER.name(), ADMIN.name());
    }
}
