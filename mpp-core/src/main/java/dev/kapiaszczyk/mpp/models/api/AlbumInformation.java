package dev.kapiaszczyk.mpp.models.api;

import java.util.Date;
import java.util.Map;

/**
 * Represents information about an album
 * hiding the implementation details.
 */
public class AlbumInformation {

    private String name;
    private String ownerUsername;
    private Date createdAt;
    private Map<String, String> usersWithAccess;

    public AlbumInformation() {
    }

    public AlbumInformation(String name, String ownerUsername, Date createdAt, Map<String, String> usersWithAccess) {
        this.name = name;
        this.ownerUsername = ownerUsername;
        this.createdAt = createdAt;
        this.usersWithAccess = usersWithAccess;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getUsersWithAccess() {
        return usersWithAccess;
    }

    public void setUsersWithAccess(Map<String, String> usersWithAccess) {
        this.usersWithAccess = usersWithAccess;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
