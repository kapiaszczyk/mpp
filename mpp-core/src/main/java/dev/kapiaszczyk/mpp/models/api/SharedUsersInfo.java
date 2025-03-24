package dev.kapiaszczyk.mpp.models.api;

/**
 * Information about users that have access to an album.
 */
public class SharedUsersInfo {

    private String id;
    private String username;
    private String permission;

    public SharedUsersInfo(String id, String username, String role) {
        this.id = id;
        this.username = username;
        this.permission = role.replaceAll("\\[ROLE_(.*?)\\]", "$1");
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPermission() {
        return permission;
    }
    
}
