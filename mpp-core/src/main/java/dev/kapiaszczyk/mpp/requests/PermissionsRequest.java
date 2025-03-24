package dev.kapiaszczyk.mpp.requests;

import dev.kapiaszczyk.mpp.models.AlbumAccessRoles;

/**
 * Represents request to change permissions of a user in an album.
 *
 * @see AlbumAccessRoles
 */
public class PermissionsRequest {

    public String userId;

    AlbumAccessRoles role;

    public PermissionsRequest(String userId, AlbumAccessRoles role) {
        this.userId = userId;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public AlbumAccessRoles getRole() {
        return role;
    }
}
