package dev.kapiaszczyk.mpp.requests;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents request to create a new album.
 */
public class AlbumCreationRequest {

    @NotBlank
    public String parentAlbumId;

    @NotBlank
    public String albumName;

    public AlbumCreationRequest(String parentAlbumId, String albumName) {
        this.parentAlbumId = parentAlbumId;
        this.albumName = albumName;
    }

    public String getParentAlbumId() {
        return parentAlbumId;
    }

    public String getAlbumName() {
        return albumName;
    }
}
