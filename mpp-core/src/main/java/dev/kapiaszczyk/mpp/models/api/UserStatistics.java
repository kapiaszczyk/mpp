package dev.kapiaszczyk.mpp.models.api;

import dev.kapiaszczyk.mpp.models.database.User;

import java.util.Map;

/**
 * Represents statistics about a user
 * visible for the administrator of the system.
 */
public class UserStatistics {

    String userId;
    String role;
    String username;
    String email;
    // Album name and occupied space
    Map<String, Long> albumStats;
    Long spaceUsed;

    public UserStatistics() {
    }

    public UserStatistics(String userId, String role, String username, String email, Map<String, Long> albumStats, Long spaceUsed) {
        this.userId = userId;
        this.role = role;
        this.username = username;
        this.email = email;
        this.albumStats = albumStats;
        this.spaceUsed = spaceUsed;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Map<String, Long> getAlbumStats() {
        return albumStats;
    }

    public void setAlbumStats(Map<String, Long> albumStats) {
        this.albumStats = albumStats;
    }

    public Long getSpaceUsed() {
        return spaceUsed;
    }

    public void setSpaceUsed(Long spaceUsed) {
        this.spaceUsed = spaceUsed;
    }

    public void setUserInfo(User user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.role = user.getRoles().get(0);
        this.email = user.getEmail();
    }
    
}
