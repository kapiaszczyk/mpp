package dev.kapiaszczyk.mpp.models.api;

import dev.kapiaszczyk.mpp.models.database.PhotoMetadata;

import java.util.List;

public class PhotoGroupedByAlbum {

    private String albumId;
    private String albumName;
    private List<PhotoMetadata> photos;

    public PhotoGroupedByAlbum(String albumId, String albumName, List<PhotoMetadata> photos) {
        this.albumId = albumId;
        this.albumName = albumName;
        this.photos = photos;
    }

    public String getAlbumId() {
        return albumId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public List<PhotoMetadata> getPhotos() {
        return photos;
    }

}
