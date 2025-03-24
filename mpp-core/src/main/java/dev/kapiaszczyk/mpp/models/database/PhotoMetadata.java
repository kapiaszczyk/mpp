package dev.kapiaszczyk.mpp.models.database;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Set;

@Document(collection = "photos")
public class PhotoMetadata {
    @Id
    private String id;
    private String filename;
    private String contentType;
    private long size;
    private Date uploadDate;
    private String userId;
    private Set<String> tags = Set.of();
    private String gridFsId;
    private String albumId;
    private String thumbnailId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public String getGridFsId() {
        return gridFsId;
    }

    public void setGridFsId(String gridFsId) {
        this.gridFsId = gridFsId;
    }

    public String getThumbnailId() {
        return thumbnailId;
    }

    public void setThumbnailId(String thumbnailId) {
        this.thumbnailId = thumbnailId;
    }

    public PhotoMetadata filename(String filename) {
        this.filename = filename;
        return this;
    }

    public PhotoMetadata contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public PhotoMetadata size(long size) {
        this.size = size;
        return this;
    }

    public PhotoMetadata uploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
        return this;
    }

    public PhotoMetadata userId(String userId) {
        this.userId = userId;
        return this;
    }

    public PhotoMetadata tags(Set<String> tags) {
        this.tags = tags;
        return this;
    }

    public PhotoMetadata gridFs(String gridFsId) {
        this.gridFsId = gridFsId;
        return this;
    }

    public PhotoMetadata albumId(String albumId) {
        this.albumId = albumId;
        return this;
    }

    public PhotoMetadata id(String id) {
        this.id = id;
        return this;
    }

    public PhotoMetadata thumbnailId(String thumbnailId) {
        this.thumbnailId = thumbnailId;
        return this;
    }

}
