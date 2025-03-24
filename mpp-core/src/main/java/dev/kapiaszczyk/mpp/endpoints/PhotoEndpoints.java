package dev.kapiaszczyk.mpp.endpoints;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * Endpoint for photo operations.
 */
@Tag(
        name = "Photo Endpoints",
        description = "Operations related to photo management. " +
                "These operations can be performed by any registered user in the system."
)
public interface PhotoEndpoints {

    /**
     * Upload a photo to the system.
     *
     * @param file          photo file
     * @param targetAlbumId id of the album where the photo should be uploaded
     * @return response containing photo id
     */
    ResponseEntity<String> uploadPhoto(MultipartFile file, String targetAlbumId);

    /**
     * Download a photo from the system.
     *
     * @param photoId id of the photo to download
     * @return response containing photo file
     */
    ResponseEntity<?> downloadPhoto(String photoId);

    /**
     * Download a thumbnail of a photo from the system.
     *
     * @param photoId id of the photo to download
     * @return response containing photo thumbnail file
     */
    ResponseEntity<?> downloadPhotoThumbnail(String photoId);

    /**
     * Get metadata of all photos in the system.
     *
     * @return response containing metadata of all photos
     */
    ResponseEntity<?> getMetadataOfPhotosInAlbum(String albumId);

    /**
     * Get metadata of all photos in the system.
     *
     * @return response containing metadata of all photos
     */
    ResponseEntity<?> getPhotoById(String photoId);

    /**
     * Delete a photo from the system.
     *
     * @param photoId id of the photo to delete
     * @return response with result of the operation
     */
    ResponseEntity<?> deletePhotoById(String photoId);

    /**
     * Move a photo to a different album.
     *
     * @param photoId id of the photo to move
     * @param albumId id of the target album
     * @return response with result of the operation
     */
    ResponseEntity<?> movePhotoToAlbum(String photoId, String albumId);

    /**
     * Duplicate a photo in the system.
     *
     * @param photoId       id of the photo to duplicate
     * @param targetAlbumId id of the album where the photo should be duplicated
     * @return response with result of the operation
     */
    ResponseEntity<?> duplicatePhotoById(String photoId, String targetAlbumId);

    /**
     * Get metadata of a photo in the system.
     *
     * @param photoId id of the photo to get metadata of
     * @return response containing metadata of the photo
     */
    ResponseEntity<?> getPhotoMetadataById(String photoId);

    /**
     * Get metadata of a photo in the system by tag.
     *
     * @param tag tag to search for
     * @return response containing metadata of the photos
     */
    ResponseEntity<?> getPhotosWithTag(String tag);

    /**
     * Get metadata of a photo in the system by tag in album.
     *
     * @param tag     tag to search for
     * @param albumId id of the album to search in
     * @return response containing metadata of the photos
     */
    ResponseEntity<?> getPhotosWithTagInAlbum(String tag, String albumId);

    /**
     * Get all tags for a user.
     *
     * @return response containing tags
     */
    ResponseEntity<?> getAllTagsForUser();

    /**
     * Get all tags for a user in album.
     *
     * @param albumId id of the album to search in
     * @return response containing tags
     */
    ResponseEntity<?> getAllTagsForUserInAlbum(String albumId);

    /**
     * Update tags for a photo.
     *
     * @param photoId id of the photo to update tags for
     * @param tags    new tags
     * @return response with result of the operation
     */
    ResponseEntity<?> updateTagsForPhoto(String photoId, Set<String> tags);

    /**
     * Get photos by tag grouped by album.
     *
     * @param tag tag to search for
     * @return response containing photo metadata grouped by album
     */
    ResponseEntity<?> getPhotosByTagGroupedByAlbum(String tag);

}
