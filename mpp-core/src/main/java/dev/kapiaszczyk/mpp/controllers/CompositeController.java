package dev.kapiaszczyk.mpp.controllers;

import dev.kapiaszczyk.mpp.constants.Urls;
import dev.kapiaszczyk.mpp.endpoints.AlbumEndpoints;
import dev.kapiaszczyk.mpp.endpoints.InternalPhotoEndpoints;
import dev.kapiaszczyk.mpp.endpoints.PhotoEndpoints;
import dev.kapiaszczyk.mpp.errors.OperationError;
import dev.kapiaszczyk.mpp.models.api.AlbumInformation;
import dev.kapiaszczyk.mpp.models.api.SharedAlbumInformation;
import dev.kapiaszczyk.mpp.models.api.SharedUsersInfo;
import dev.kapiaszczyk.mpp.models.database.Album;
import dev.kapiaszczyk.mpp.models.database.PhotoMetadata;
import dev.kapiaszczyk.mpp.models.database.User;
import dev.kapiaszczyk.mpp.requests.AlbumCreationRequest;
import dev.kapiaszczyk.mpp.requests.AlbumDeletionRequest;
import dev.kapiaszczyk.mpp.requests.PermissionsRequest;
import dev.kapiaszczyk.mpp.responses.PhotoResponseParts;
import dev.kapiaszczyk.mpp.services.CompositeService;
import dev.kapiaszczyk.mpp.util.Either;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * Controller for handling operations related to albums and photos.
 */
@RestController
public class CompositeController implements AlbumEndpoints, PhotoEndpoints, InternalPhotoEndpoints {

    @Autowired
    CompositeService compositeService;

    /**
     * Create a new album
     *
     * @param request request containing album data
     * @return response containing the result of the operation
     */
    @PostMapping(Urls.ALBUMS_URL_PREFIX)
    public ResponseEntity<?> createAlbum(@RequestBody AlbumCreationRequest request) {
        Either<String, Album> result = compositeService.createAlbum(request.parentAlbumId, request.albumName);
        if (result.isLeft()) {
            return ResponseEntity.badRequest().body(result.left().orElse("Unknown error"));
        } else {
            return ResponseEntity.ok().body("Album created");
        }
    }

    /**
     * Delete an album by its id
     *
     * @param albumId id of the album to delete
     * @param request request containing data about moving children data to parent album
     * @return response containing the result of the operation
     */
    @DeleteMapping(Urls.ALBUMS_URL_PREFIX + "/{albumId}")
    public ResponseEntity<?> deleteAlbum(@PathVariable String albumId, @RequestBody AlbumDeletionRequest request) {
        // TODO: Handle the case in which the album contains photos that were uploaded by users who have shared access
        Either<OperationError, ?> result = compositeService.deleteById(albumId, request.moveChildrenDataToParent(), request.movePhotosToParentAlbum());
        if (result.isLeft()) {
            return ResponseEntity.status(result.left().get().getHttpStatus()).body(result.left().get().getMessage());
        } else {
            return ResponseEntity.ok().body("Album deleted");
        }
    }

    /**
     * Set permissions for a user on an album
     *
     * @param albumId id of the album
     * @param request request containing user id and role
     * @return response containing the result of the operation
     */
    @PostMapping(Urls.ALBUMS_URL_PREFIX + "/{albumId}" + Urls.PERMISSIONS_URL)
    public ResponseEntity<?> setPermissions(@PathVariable String albumId, @RequestBody PermissionsRequest request) {
        Either<String, String> result = compositeService.addAccessToAlbum(albumId, request.getUserId(), request.getRole());
        if (result.isLeft()) {
            return ResponseEntity.badRequest().body(result.left().orElse("Unknown error"));
        } else {
            return ResponseEntity.ok().body("Permissions updated");
        }
    }

    /**
     * Get all permissions for an album
     *
     * @param albumId id of the album
     * @return response containing the result of the operation
     */
    @GetMapping(Urls.ALBUMS_URL_PREFIX + "/{albumId}" + Urls.PERMISSIONS_URL)
    public ResponseEntity<?> getPermissions(@PathVariable String albumId) {
        Either<String, ?> result = compositeService.getPermissions(albumId);
        if (result.isLeft()) {
            return ResponseEntity.badRequest().body(result.left().orElse("Unknown error"));
        } else {
            return ResponseEntity.ok().body(result.right());
        }
    }

    /**
     * Edit permissions for a user on an album
     *
     * @param albumId id of the album
     * @param request request containing user id and role
     * @return response containing the result of the operation
     */
    @PutMapping(Urls.ALBUMS_URL_PREFIX + "/{albumId}" + Urls.PERMISSIONS_URL)
    public ResponseEntity<?> editPermissions(@PathVariable String albumId, @RequestBody PermissionsRequest request) {
        // TODO: If shared user's access changes, the photos remain in the album and are not moved
        Either<String, String> result = compositeService.editAccessToAlbum(albumId, request.getUserId(), request.getRole());
        if (result.isLeft()) {
            return ResponseEntity.badRequest().body(result.left().orElse("Unknown error"));
        } else {
            return ResponseEntity.ok().body("Permissions updated");
        }
    }

    /**
     * Remove permissions for a user on an album
     *
     * @param albumId id of the album
     * @param userId  id of the user
     * @return response containing the result of the operation
     */
    @DeleteMapping(Urls.ALBUMS_URL_PREFIX + "/{albumId}" + Urls.PERMISSIONS_URL + "/{userId}")
    public ResponseEntity<?> removePermissions(@PathVariable String albumId, @PathVariable String userId) {
        // TODO: If shared user's access changes, the photos remain in the album and are not moved
        Either<String, String> result = compositeService.removeAccessToAlbum(albumId, userId);
        if (result.isLeft()) {
            return ResponseEntity.badRequest().body(result.left().orElse("Unknown error"));
        } else {
            return ResponseEntity.ok().body("Permissions updated");
        }
    }

    /**
     * Get all children of an album
     *
     * @param albumId id of the album
     * @return response containing the result of the operation
     */
    @GetMapping(Urls.ALBUMS_URL_PREFIX + "/{albumId}/children")
    public ResponseEntity<?> getChildren(@PathVariable String albumId) {
        Either<String, List<Album>> result = compositeService.getChildrenOfAlbum(albumId);
        if (result.isLeft()) {
            return ResponseEntity.badRequest().body(result.left().orElse("Unknown error"));
        } else {
            return ResponseEntity.ok().body(result.right());
        }
    }

    /**
     * Get all shared users for an album
     *
     * @param albumId id of the album
     * @return response containing the result of the operation
     */
    @GetMapping(Urls.ALBUMS_URL_PREFIX + "/{albumId}/shared-users")
    public ResponseEntity<?> getSharedUsers(@PathVariable String albumId) {
        Either<OperationError, List<SharedUsersInfo>> result = compositeService.getSharedUsers(albumId);
        if (result.isLeft()) {
            return ResponseEntity.status(result.left().get().getHttpStatus()).body(result.left().get().getMessage());
        } else {
            return ResponseEntity.ok().body(result.right());
        }
    }

    /**
     * Get all shared albums for the user who authenticated the request
     *
     * @return response with all shared albums
     */
    @GetMapping(Urls.ALBUMS_URL_PREFIX + "/shared")
    public ResponseEntity<?> getSharedAlbums() {
        Either<OperationError, List<SharedAlbumInformation>> result = compositeService.getSharedAlbums();
        if (result.isLeft()) {
            return ResponseEntity.status(result.left().get().getHttpStatus()).body(result.left().get().getMessage());
        } else {
            return ResponseEntity.ok().body(result.right());
        }
    }

    /**
     * Get all albums for the user who authenticated the request
     *
     * @return response with all albums
     */
    @GetMapping(Urls.ALBUMS_URL_PREFIX + "/all")
    public ResponseEntity<?> getAllAlbums() {
        Either<OperationError, List<Album>> result = compositeService.getAllAlbums();
        if (result.isLeft()) {
            return ResponseEntity.status(result.left().get().getHttpStatus()).body(result.left().get().getMessage());
        } else {
            return ResponseEntity.ok().body(result.right());
        }
    }

    /**
     * Get information about a public album
     *
     * @param albumId id of the album
     * @return response with album information
     */
    @GetMapping(Urls.ALBUMS_URL_PREFIX + "/{albumId}/info")
    public ResponseEntity<?> getPublicAlbumInformation(@PathVariable String albumId) {
        Either<OperationError, AlbumInformation> result = compositeService.getPublicAlbumInformation(albumId);
        if (result.isLeft()) {
            return ResponseEntity.status(result.left().get().getHttpStatus()).body(result.left().get().getMessage());
        } else {
            return ResponseEntity.ok().body(result.right());
        }
    }

    /**
     * Get information about a shared album
     *
     * @param albumId id of the album
     * @return response with album information
     */
    @GetMapping(Urls.ALBUMS_URL_PREFIX + "/shared/{albumId}")
    public ResponseEntity<?> getInformationAboutSharedAlbum(@PathVariable String albumId) {
        Either<OperationError, SharedAlbumInformation> result = compositeService.getInformationAboutSharedAlbum(albumId);
        if (result.isLeft()) {
            return ResponseEntity.status(result.left().get().getHttpStatus()).body(result.left().get().getMessage());
        } else {
            return ResponseEntity.ok().body(result.right());
        }
    }

    /**
     * Get all albums owned by the user
     *
     * @return response with all albums
     */
    @GetMapping(Urls.ALBUMS_URL_PREFIX + "/tree")
    public ResponseEntity<?> getAllAlbumsOwnedByUser() {
        return ResponseEntity.ok().body(compositeService.getAllAlbumsOwnedByUser());
    }

    /**
     * Get an album by its ID
     *
     * @param albumId id of the album
     * @return response with the album
     */
    @GetMapping(Urls.ALBUMS_URL_PREFIX + "/{albumId}")
    public ResponseEntity<?> getAlbumById(@PathVariable String albumId) {
        Either<OperationError, Album> result = compositeService.getAlbumById(albumId);
        if (result.isLeft()) {
            return ResponseEntity.status(result.left().get().getHttpStatus()).body(result.left().get().getMessage());
        } else {
            return ResponseEntity.ok().body(result.rightOrElse(null));
        }
    }

    /**
     * Get the root album for the user who authenticated the request
     *
     * @return response with the root album
     */
    @GetMapping(Urls.ALBUMS_URL_PREFIX + "/root")
    public ResponseEntity<?> getRootAlbum() {
        Either<OperationError, Album> result = compositeService.getRootAlbum();
        if (result.isLeft()) {
            return ResponseEntity.status(result.left().get().getHttpStatus()).body(result.left().get().getMessage());
        } else {
            return ResponseEntity.ok().body(result.rightOrElse(null));
        }
    }

    /**
     * Get breadcrumbs - pairs of ids and names of albums that form the path to the current album
     *
     * @param albumId id of the album
     * @return response
     */
    @GetMapping(Urls.ALBUMS_URL_PREFIX + "/path" + "/{albumId}")
    public ResponseEntity<?> getAncestorAlbums(@PathVariable String albumId) {
        Either<String, List<Album>> result = compositeService.getAncestorAlbums(albumId);
        if (result.isLeft()) {
            return ResponseEntity.status(403).body(result.left().orElse(""));
        } else {
            return ResponseEntity.ok().body(result.right());
        }
    }

    /**
     * Upload a photo
     *
     * @param file          photo file
     * @param targetAlbumId id of the album to upload the photo to
     * @return response containing the result of the operation
     */
    @PostMapping(Urls.PHOTOS_URL_PREFIX + "/upload")
    public ResponseEntity<String> uploadPhoto(@RequestParam("file") MultipartFile file, @RequestParam("targetAlbumId") String targetAlbumId) {
        Either<OperationError, String> result = compositeService.uploadPhoto(file, targetAlbumId);
        if (result.isLeft()) {
            return ResponseEntity.status(result.left().get().getHttpStatus()).body(result.left().get().getMessage());
        } else {
            return ResponseEntity.ok().body(result.right().get());
        }
    }

    /**
     * Download a photo by its ID
     *
     * @param photoId id of the photo
     * @return response containing the photo
     */
    @GetMapping(Urls.PHOTOS_URL_PREFIX + "/download/{photoId}")
    public ResponseEntity<StreamingResponseBody> downloadPhoto(@PathVariable String photoId) {
        Either<OperationError, PhotoResponseParts> result = compositeService.downloadPhoto(photoId);

        if (result.isLeft()) {
            return ResponseEntity.status(result.left().get().getHttpStatus()).body(null);
        }

        PhotoResponseParts photoResponse = result.right().get();
        StreamingResponseBody responseBody = outputStream -> {
            try (InputStream inputStream = photoResponse.getResource().getInputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    outputStream.flush();
                }
            } catch (IOException e) {
                throw new RuntimeException("Error streaming file", e);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + photoResponse.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(photoResponse.getContentType()))
                .contentLength(photoResponse.getSize())
                .header(HttpHeaders.CONNECTION, "keep-alive")
                .body(responseBody);
    }


    /**
     * Download a thumbnail of a photo
     *
     * @param photoId id of the photo
     * @return response containing the thumbnail
     */
    @GetMapping(Urls.PHOTOS_URL_PREFIX + "/download/{photoId}/thumbnail")
    public ResponseEntity<?> downloadPhotoThumbnail(@PathVariable String photoId) {
        Either<OperationError, ResponseEntity<?>> result = compositeService.downloadThumbnail(photoId);
        if (result.isLeft()) {
            return ResponseEntity.status(result.left().get().getHttpStatus()).body(result.left().get().getMessage());
        } else {
            return result.rightOrElse(ResponseEntity.internalServerError().body("Unknown error"));
        }
    }

    /**
     * Download a photo (internal)
     *
     * @param photoId id of the photo
     * @return response containing the photo
     */
    @Hidden
    @GetMapping(Urls.PHOTOS_URL_PREFIX + "/internal/photo/{photoId}")
    public ResponseEntity<?> downloadPhotoInternal(@PathVariable String photoId) {
        return compositeService.downloadPhotoInternal(photoId)
                .flatMap(this::buildResponse)
                .fold(
                        error -> ResponseEntity.status(error.getHttpStatus()).body(error.getMessage()),
                        response -> response
                );
    }

    /**
     * Get metadata of all photos in an album
     *
     * @param albumId id of the album
     * @return response containing the metadata
     */
    @GetMapping(Urls.PHOTOS_URL_PREFIX + "/album/{albumId}")
    public ResponseEntity<?> getMetadataOfPhotosInAlbum(@PathVariable String albumId) {
        Either<String, List<PhotoMetadata>> result = compositeService.getMetadataOfPhotosInAlbum(albumId);
        if (result.isLeft()) {
            return ResponseEntity.status(403).body(result.left().orElse(""));
        } else {
            return ResponseEntity.ok(result.right());
        }
    }

    /**
     * Get a photo by its ID
     *
     * @param photoId id of the photo
     * @return response containing the photo metadata
     */
    @GetMapping(Urls.PHOTOS_URL_PREFIX + "/{photoId}")
    public ResponseEntity<?> getPhotoById(@PathVariable String photoId) {
        Either<String, ?> result = compositeService.getPhotoById(photoId);
        if (result.isLeft()) {
            return ResponseEntity.status(403).body(result.left().orElse(""));
        } else {
            return ResponseEntity.ok(result.right());
        }
    }

    /**
     * Delete a photo by its ID
     *
     * @param photoId id of the photo
     * @return response containing the result of the operation
     */
    @DeleteMapping(Urls.PHOTOS_URL_PREFIX + "/{photoId}")
    public ResponseEntity<?> deletePhotoById(@PathVariable String photoId) {
        Either<OperationError, String> result = compositeService.deletePhotoById(photoId);
        if (result.isLeft()) {
            return ResponseEntity.status(result.left().get().getHttpStatus()).body(result.left().get().getMessage());
        } else {
            return ResponseEntity.ok(result.right());
        }
    }

    /**
     * Move a photo to another album
     *
     * @param photoId       id of the photo
     * @param targetAlbumId id of the target album
     * @return response containing the result of the operation
     */
    @PutMapping(Urls.PHOTOS_URL_PREFIX + "/{photoId}/move/{targetAlbumId}")
    public ResponseEntity<?> movePhotoToAlbum(@PathVariable String photoId, @PathVariable String targetAlbumId) {
        Either<OperationError, String> result = compositeService.movePhotoToAlbum(photoId, targetAlbumId);
        if (result.isLeft()) {
            return ResponseEntity.status(result.left().get().getHttpStatus()).body(result.left().get().getMessage());
        } else {
            return ResponseEntity.ok(result.right());
        }
    }

    /**
     * Duplicate a photo to another album
     *
     * @param photoId       id of the photo
     * @param targetAlbumId id of the target album
     * @return response containing the result of the operation
     */
    @PostMapping(Urls.PHOTOS_URL_PREFIX + "/{photoId}/duplicate/{targetAlbumId}")
    public ResponseEntity<?> duplicatePhotoById(@PathVariable String photoId, @PathVariable String targetAlbumId) {
        Either<String, String> result = compositeService.duplicatePhotoById(photoId, targetAlbumId);
        if (result.isLeft()) {
            return ResponseEntity.status(403).body(result.left().orElse(""));
        } else {
            return ResponseEntity.ok(result.right());
        }
    }

    /**
     * Get metadata of a photo by its ID
     *
     * @param photoId id of the photo
     * @return response containing the metadata
     */
    @GetMapping(Urls.PHOTOS_URL_PREFIX + "/metadata/{photoId}")
    public ResponseEntity<?> getPhotoMetadataById(@PathVariable String photoId) {
        Either<String, PhotoMetadata> result = compositeService.getPhotoMetadataById(photoId);
        if (result.isLeft()) {
            return ResponseEntity.status(403).body(result.left().orElse(""));
        } else {
            return ResponseEntity.ok(result.right());
        }
    }

    /**
     * Get all images of a user with a given tag
     *
     * @param tag tag to search for
     * @return response containing the photos metadata
     */
    @GetMapping(Urls.PHOTOS_URL_PREFIX + "/tag/{tag}")
    public ResponseEntity<?> getPhotosWithTag(@PathVariable String tag) {
        Either<String, List<PhotoMetadata>> result = this.compositeService.getPhotosWithTag(tag);
        if (result.isLeft()) {
            return ResponseEntity.status(403).body(result.left().orElse(""));
        } else {
            return ResponseEntity.ok(result.right());
        }
    }

    /**
     * Get all images of a user with a given tag present in a specified album
     *
     * @param tag     tag to search for
     * @param albumId id of the album
     * @return response containing the photos metadata
     */
    @GetMapping(Urls.PHOTOS_URL_PREFIX + "/tag/{tag}/folder/{albumId}")
    public ResponseEntity<?> getPhotosWithTagInAlbum(@PathVariable String tag, @PathVariable String albumId) {
        Either<String, List<PhotoMetadata>> result = this.compositeService.getPhotosWithTagInAlbum(tag, albumId);
        if (result.isLeft()) {
            return ResponseEntity.status(403).body(result.left().orElse(""));
        } else {
            return ResponseEntity.ok(result.right());
        }
    }

    /**
     * Get all tags for a user
     *
     * @return response with tags
     */
    @GetMapping(Urls.PHOTOS_URL_PREFIX + "/tags")
    public ResponseEntity<?> getAllTagsForUser() {
        Either<String, List<String>> result = this.compositeService.getAllTagsForUser();
        if (result.isLeft()) {
            return ResponseEntity.status(403).body(result.left().orElse(""));
        } else {
            return ResponseEntity.ok(result.right());
        }
    }

    /**
     * Get all tags for a user in a folder
     *
     * @param albumId id of the folder
     * @return response with tags
     */
    @GetMapping(Urls.PHOTOS_URL_PREFIX + "/tags/folder/{albumId}")
    public ResponseEntity<?> getAllTagsForUserInAlbum(@PathVariable String albumId) {
        Either<String, List<String>> result = compositeService.getAllTagsForUserInAlbum(albumId);
        if (result.isLeft()) {
            return ResponseEntity.status(403).body(result.left().orElse(""));
        } else {
            return ResponseEntity.ok(result.right());
        }
    }

    /**
     * Update tags for a photo
     *
     * @param photoId id of the photo
     * @param tags    new tags
     * @return response with the result of the operation
     */
    @PutMapping(Urls.PHOTOS_URL_PREFIX + "/{photoId}/tags")
    public ResponseEntity<?> updateTagsForPhoto(@PathVariable String photoId, @RequestBody Set<String> tags) {
        Either<OperationError, String> result = compositeService.updateTags(photoId, tags);
        if (result.isLeft()) {
            return ResponseEntity.status(result.left().get().getHttpStatus()).body(result.left().get().getMessage());
        } else {
            return ResponseEntity.ok(result.right());
        }
    }

    /**
     * Get all photos grouped by tags
     *
     * @param tag tag to group by
     * @return response with the photos
     */
    @GetMapping(Urls.PHOTOS_URL_PREFIX + "/tag/{tag}/grouped")
    public ResponseEntity<?> getPhotosByTagGroupedByAlbum(@PathVariable String tag) {
        // TODO: Add error handling
        return ResponseEntity.ok().body(compositeService.getPhotoMetadataByTagGroupedByAlbums(tag));
    }

    /**
     * Set photo as album cover
     *
     * @param albumId id of the album
     * @param photoId id of the photo
     * @return response containing the result of the operation
     */
    @PostMapping(Urls.ALBUMS_URL_PREFIX + "/{albumId}/photo/{photoId}/cover")
    public ResponseEntity<?> setPhotoAsAlbumCover(@PathVariable String albumId, @PathVariable String photoId) {
        // TODO - The thumbnails are not cleared from the albums when the photo is deleted
        Either<OperationError, String> result = compositeService.setPhotoAsAlbumCover(albumId, photoId);
        if (result.isLeft()) {
            return ResponseEntity.status(result.left().get().getHttpStatus()).body(result.left().get().getMessage());
        } else {
            return ResponseEntity.ok(result.right());
        }
    }

    /**
     * Remove photo as album cover
     *
     * @param albumId id of the album
     * @return response containing the result of the operation
     */
    @DeleteMapping(Urls.ALBUMS_URL_PREFIX + "/{albumId}/cover")
    public ResponseEntity<?> removePhotoAsAlbumCover(@PathVariable String albumId) {
        Either<OperationError, String> result = compositeService.removePhotoAsAlbumCover(albumId);
        if (result.isLeft()) {
            return ResponseEntity.status(result.left().get().getHttpStatus()).body(result.left().get().getMessage());
        } else {
            return ResponseEntity.ok(result.right());
        }
    }

    /**
     * Rename an album
     *
     * @param albumId album id
     * @param newName id of the album
     * @return response containing the result of the operation
     */
    @PutMapping(Urls.ALBUMS_URL_PREFIX + "/{albumId}/rename")
    public ResponseEntity<?> renameAlbum(@PathVariable String albumId, @RequestBody String newName) {
        Either<OperationError, String> result = compositeService.renameAlbum(albumId, newName);
        if (result.isLeft()) {
            return ResponseEntity.status(result.left().get().getHttpStatus()).body(result.left().get().getMessage());
        } else {
            return ResponseEntity.ok(result.right());
        }
    }

    /**
     * Search for users by their name
     *
     * @param query search query
     * @return list of users matching the query
     */
    @GetMapping("/users/search")
    public List<User> searchUsers(@RequestParam String query) {
        return compositeService.searchUsers(query);
    }

    private Either<OperationError, ResponseEntity<InputStreamResource>> buildResponse(PhotoResponseParts photoResponseParts) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(photoResponseParts.getContentType()));
        headers.setContentDispositionFormData("attachment", photoResponseParts.getFilename());
        headers.setContentLength(photoResponseParts.getSize());

        ResponseEntity<InputStreamResource> responseEntity = ResponseEntity.ok()
                .headers(headers)
                .body(photoResponseParts.getResource());

        return Either.ofRight(responseEntity);
    }

}
