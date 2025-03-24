package dev.kapiaszczyk.mpp.requests;

import org.springframework.web.multipart.MultipartFile;

/**
 * Represents request to upload a photo.
 */
public class PhotoUploadRequest {

    MultipartFile file;

    /**
     * Album id where the photo should be uploaded.
     */
    String targetAlbumId;

    public PhotoUploadRequest(MultipartFile file, String targetAlbumId) {
        this.file = file;
        this.targetAlbumId = targetAlbumId;
    }

    public MultipartFile getFile() {
        return file;
    }

    public String getTargetAlbumId() {
        return targetAlbumId;
    }
}
