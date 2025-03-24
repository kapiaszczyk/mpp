package dev.kapiaszczyk.mpp.models.database;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an album in the database.
 */
@Document(collection = "albums")
public class Album {

    @Id
    private String id;
    private String name;
    private String parentId;
    private boolean isRoot;
    private String path;
    private Map<String, String> permissions;
    private String ownerId;
    private Date createdAt;
    private long photoCount;
    private String thumbnailId;

    public Album() {
        this.permissions = new HashMap<>();
    }

    public Album(String name, String parentId, String path, boolean isRoot, String owner, Date createdAt) {
        this.name = name;
        this.parentId = parentId;
        this.path = path;
        this.isRoot = isRoot;
        this.ownerId = owner;
        this.createdAt = createdAt;
        this.permissions = new HashMap<>();
        this.photoCount = 0;
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

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public void setRoot(boolean root) {
        isRoot = root;
    }

    public Map<String, String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, String> permissions) {
        this.permissions = permissions;
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

    public long getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(long photoCount) {
        this.photoCount = photoCount;
    }

    public String getThumbnailId() {
        return thumbnailId;
    }

    public void setThumbnailId(String thumbnailId) {
        this.thumbnailId = thumbnailId;
    }

    public String[] getNamesOfAlbumsFromPath() {
        return Arrays.stream(this.path.split("/")).filter(s -> !s.isEmpty()).toArray(String[]::new);
    }

    public static Album createFromParent(String name, Album parent) {
        return new Album(name, parent.getId(), parent.getPath() + "/" + name, false, parent.getOwnerId(), new Date());
    }

    @Override
    public String toString() {
        return "Album{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", parentId='" + parentId + '\'' +
                ", isRoot=" + isRoot +
                ", path='" + path + '\'' +
                ", permissions=" + permissions +
                ", ownerId='" + ownerId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    public void incrementPhotoCount() {
        this.photoCount++;
    }

    public void decrementPhotoCount() {
        this.photoCount--;
    }

    public void incrementPhotoCountBy(long count) {
        this.photoCount += count;
    }

    public void decrementPhotoCountBy(long count) {
        if (this.photoCount - count < 0) {
            this.photoCount = 0;
        } else {
            this.photoCount -= count;
        }
    }

    public void renameAlbum(String newName) {
        this.name = newName;
        // Replace the last part in the path /root/album1/album2 -> /root/album1/newName
        String[] pathParts = this.path.split("/");
        pathParts[pathParts.length - 1] = newName;
        this.path = String.join("/", pathParts);
    }
}
