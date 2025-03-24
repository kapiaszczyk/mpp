package dev.kapiaszczyk.mpp.endpoints;

import dev.kapiaszczyk.mpp.requests.AlbumCreationRequest;
import dev.kapiaszczyk.mpp.requests.AlbumDeletionRequest;
import dev.kapiaszczyk.mpp.requests.PermissionsRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * Endpoint for album operations.
 */
@Tag(
        name = "Album Endpoints",
        description = "Operations related to album management. " +
                "These operations can be performed by any registered user in the system."
)
public interface AlbumEndpoints {

    /**
     * Create a new album.
     *
     * @param request request containing album name and parent album id
     * @return operation result
     */
    ResponseEntity<?> createAlbum(AlbumCreationRequest request);

    /**
     * Delete an album.
     *
     * @param albumId id of the album to delete
     * @param request request containing album deletion options
     * @return operation result
     */
    ResponseEntity<?> deleteAlbum(String albumId, AlbumDeletionRequest request);

    /**
     * Set permissions for an album.
     *
     * @param albumId id of the album to set permissions for
     * @param request request containing permissions
     * @return operation result
     */
    ResponseEntity<?> setPermissions(String albumId, PermissionsRequest request);

    /**
     * Get permissions for an album.
     *
     * @param albumId id of the album to get permissions for
     * @return response containing permissions
     */
    ResponseEntity<?> getPermissions(String albumId);

    /**
     * Edit permissions for an album.
     *
     * @param albumId id of the album to edit permissions for
     * @param request request containing permissions
     * @return operation result
     */
    ResponseEntity<?> editPermissions(String albumId, PermissionsRequest request);

    /**
     * Remove permissions for an album.
     *
     * @param albumId  id of the album to remove permissions for
     * @param username username to remove permissions for
     * @return operation result
     */
    ResponseEntity<?> removePermissions(String albumId, String username);

    /**
     * Get children of an album.
     *
     * @param albumId id of the album to get children of
     * @return response containing list of children
     */
    ResponseEntity<?> getChildren(String albumId);

    /**
     * Get all albums owned by the user.
     *
     * @return response containing list of children albums
     */
    ResponseEntity<?> getAllAlbumsOwnedByUser();

    /**
     * Get album that is the root of the user's album tree.
     *
     * @return response containing the root album
     */
    ResponseEntity<?> getRootAlbum();

    /**
     * Get ancestor albums of an album.
     *
     * @param albumId id of the album to get ancestors of
     * @return response containing list of ancestor albums
     */
    ResponseEntity<?> getAncestorAlbums(String albumId);

    /**
     * Get album by id.
     *
     * @param albumId id of the album to get
     * @return response containing the album
     */
    ResponseEntity<?> getAlbumById(String albumId);

    /**
     * Get users who have access to an album.
     *
     * @param albumId id of the album to share
     * @return response containing list of shared users
     */
    ResponseEntity<?> getSharedUsers(String albumId);

    /**
     * Get information about a shared album.
     *
     * @param albumId id of the album to get information about
     * @return response containing information about the shared album
     */
    ResponseEntity<?> getInformationAboutSharedAlbum(String albumId);

    /**
     * Get albums shared with the user.
     *
     * @return response containing list of shared albums
     */
    ResponseEntity<?> getSharedAlbums();

    /**
     * Get all albums - owned and shared with user
     *
     * @return response containing list of all albums
     */
    ResponseEntity<?> getAllAlbums();

    /**
     * Get information about an album that contains no sensitive data.
     *
     * @param albumId id of the album to share
     * @return response containing information about the album
     */
    ResponseEntity<?> getPublicAlbumInformation(String albumId);

    /**
     * Set a photo as the cover of an album.
     *
     * @param albumId id of the album
     * @param photoId id of the photo
     * @return response with result of the operation
     */
    ResponseEntity<?> setPhotoAsAlbumCover(String albumId, String photoId);

    /**
     * Remove the cover photo from an album.
     *
     * @param albumId id of the album
     * @return response with result of the operation
     */
    ResponseEntity<?> removePhotoAsAlbumCover(String albumId);

    /**
     * Rename an album.
     *
     * @param albumId id of the album
     * @param newName new name of the album
     * @return response with result of the operation
     */
    ResponseEntity<?> renameAlbum(String albumId, String newName);


}
