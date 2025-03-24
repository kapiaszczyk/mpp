package dev.kapiaszczyk.mpp.models.api;

import dev.kapiaszczyk.mpp.models.database.Album;

import java.util.Date;

/**
 * Represents information available about an album
 * that is shared with the current user.
 */
public class SharedAlbumInformation {

    private String id;
    private String name;
    private String roleOfCurrentUser;
    private String ownerId;
    private Date createdAt;
    private String thumbnailId;

    public SharedAlbumInformation() {
    }

    public SharedAlbumInformation(String id, String name, String roleOfCurrentUser, String ownerId, Date createdAt, String thumbnailId) {
        this.id = id;
        this.name = name;
        this.roleOfCurrentUser = roleOfCurrentUser;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
        this.thumbnailId = thumbnailId;
    }

    public static SharedAlbumInformation mapAlbumToSharedAlbumInfo(Album album, String userId) {
        String role = album.getPermissions().get(userId);
        return new SharedAlbumInformation(
                album.getId(),
                album.getName(),
                role,
                album.getOwnerId(),
                album.getCreatedAt(),
                album.getThumbnailId()
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoleOfCurrentUser() {
        return roleOfCurrentUser;
    }

    public void setRoleOfCurrentUser(String roleOfCurrentUser) {
        this.roleOfCurrentUser = roleOfCurrentUser;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getThumbnailId() {
        return thumbnailId;
    }

    public void setThumbnailId(String thumbnailId) {
        this.thumbnailId = thumbnailId;
    }
}
