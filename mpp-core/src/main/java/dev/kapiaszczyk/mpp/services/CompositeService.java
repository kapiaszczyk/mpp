package dev.kapiaszczyk.mpp.services;

import dev.kapiaszczyk.mpp.constants.Constants;
import dev.kapiaszczyk.mpp.errors.OperationError;
import dev.kapiaszczyk.mpp.models.AlbumAccessRoles;
import dev.kapiaszczyk.mpp.models.api.AlbumInformation;
import dev.kapiaszczyk.mpp.models.api.PhotoGroupedByAlbum;
import dev.kapiaszczyk.mpp.models.api.SharedAlbumInformation;
import dev.kapiaszczyk.mpp.models.api.SharedUsersInfo;
import dev.kapiaszczyk.mpp.models.database.Album;
import dev.kapiaszczyk.mpp.models.database.PhotoMetadata;
import dev.kapiaszczyk.mpp.models.database.User;
import dev.kapiaszczyk.mpp.responses.PhotoDownloadResponse;
import dev.kapiaszczyk.mpp.responses.PhotoResponseParts;
import dev.kapiaszczyk.mpp.util.Either;
import jakarta.validation.constraints.NotBlank;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static dev.kapiaszczyk.mpp.errors.GenericErrors.*;

/**
 * Service that combines the functionality of the PhotoService and AlbumService
 */
@Service
public class CompositeService {

    private static final Logger logger = LoggerFactory.getLogger(CompositeService.class);

    @Autowired
    private final PhotoService photoService;

    @Autowired
    private final AlbumService albumService;

    @Autowired
    private final PermissionsService permissionsService;

    public CompositeService(PhotoService photoService, AlbumService albumService, PermissionsService permissionsService) {
        this.photoService = photoService;
        this.albumService = albumService;
        this.permissionsService = permissionsService;
    }

    /**
     * Upload a photo to the specified album
     *
     * @param file          The photo file
     * @param targetAlbumId The album to upload the photo to
     * @return The id of the uploaded photo
     */
    public Either<OperationError, String> uploadPhoto(MultipartFile file, String targetAlbumId) {
        if (!photoService.isFileValid(file)) {
            return Either.ofLeft(OperationError.badRequest(INVALID_FILE_FORMAT));
        }

        Optional<Album> targetAlbum = albumService.findById(targetAlbumId);
        if (targetAlbum.isEmpty()) {
            return Either.ofLeft(OperationError.notFound(NO_SUCH_ALBUM_EXISTS));
        }
        Album album = targetAlbum.get();
        if (!this.permissionsService.isOwnerAdminOrEditorOrViewer(album)) {
            return Either.ofLeft(OperationError.forbidden(NO_PERMISSION_TO_UPLOAD_PHOTO));
        }

        User user = this.permissionsService.getUserFromCtx();
        String userIdFromCtx = user.getId();

        try {
            String fileId = this.photoService.uploadPhoto(file, userIdFromCtx, targetAlbumId);
            albumService.incrementPhotoCount(targetAlbumId);
            return Either.ofRight(fileId);
        } catch (Exception e) {
            return Either.ofLeft(OperationError.internalServerError(FAILED_TO_UPLOAD_PHOTO + ": " + e.getMessage()));
        }
    }

    /**
     * Return the photo with the specified id
     *
     * @param photoId The id of the photo to download
     * @return The photo
     */
    public Either<OperationError, PhotoResponseParts> downloadPhoto(String photoId) {
        if (photoIdIsInvalid(photoId)) {
            return Either.ofLeft(OperationError.badRequest("Invalid photo ID"));
        }

        Optional<PhotoMetadata> metadata = this.photoService.getPhotoById(photoId);

        if (metadata.isEmpty()) {
            return Either.ofLeft(OperationError.notFound(PHOTO_NOT_FOUND));
        }

        Album album = albumService.findById(metadata.get().getAlbumId()).orElseThrow(() -> new IllegalArgumentException(NO_SUCH_ALBUM_EXISTS));

        if (!this.permissionsService.isOwnerAdminOrEditorOrViewer(album)) {
            return Either.ofLeft(OperationError.forbidden(NO_PERMISSION_TO_DOWNLOAD_PHOTO));
        }

        try {
            PhotoDownloadResponse photo = this.photoService.downloadPhoto(metadata.get().getGridFsId());

            String filename = photo.getMetadata().getFilename();
            long size = photo.getMetadata().getLength();
            InputStreamResource resource = new InputStreamResource(photo.getFile());
            String contentType = determineContentType(filename);

            PhotoResponseParts response = new PhotoResponseParts(resource, filename, contentType, size);

            return Either.ofRight(response);
        } catch (Exception e) {
            return Either.ofLeft(OperationError.internalServerError(e.getMessage()));
        }
    }

    /**
     * Download the thumbnail of the photo with the specified id
     *
     * @param photoId The id of the photo
     * @return The thumbnail
     */
    public Either<OperationError, ResponseEntity<?>> downloadThumbnail(String photoId) {
        if (photoIdIsInvalid(photoId)) {
            return Either.ofLeft(OperationError.badRequest("Invalid photo ID"));
        }

        try {
            Optional<PhotoMetadata> metadata = this.photoService.getPhotoById(photoId);

            if (metadata.isEmpty()) {
                return Either.ofLeft(OperationError.notFound(PHOTO_NOT_FOUND));
            }

            PhotoDownloadResponse photo = this.photoService.downloadThumbnail(metadata.get().getThumbnailId());

            String filename = photo.getMetadata().getFilename();
            long size = photo.getMetadata().getLength();
            InputStreamResource resource = new InputStreamResource(photo.getFile());

            String contentType = determineContentType(filename);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(size);

            return Either.ofRight(ResponseEntity.ok()
                    .headers(headers)
                    .body(resource));
        } catch (Exception e) {
            return Either.ofLeft(OperationError.internalServerError(e.getMessage()));
        }
    }

    /**
     * Download the photo with the specified id. This is used by internal services.
     *
     * @param photoId The id of the photo
     * @return The photo
     */
    public Either<OperationError, PhotoResponseParts> downloadPhotoInternal(String photoId) {
        // TODO: Temporary solution for the tagging service
        if (!SecurityContextHolder.getContext().getAuthentication().getName().equals(Constants.TAG_SERVICE_NAME)) {
            return Either.ofLeft(OperationError.forbidden(NO_PERMISSION_TO_DOWNLOAD_PHOTO));
        }

        try {
            Optional<PhotoMetadata> metadata = this.photoService.getPhotoById(photoId);

            if (metadata.isEmpty()) {
                return Either.ofLeft(OperationError.notFound(PHOTO_NOT_FOUND));
            }

            PhotoDownloadResponse photo = this.photoService.downloadPhoto(metadata.get().getGridFsId());

            String filename = photo.getMetadata().getFilename();
            long size = photo.getMetadata().getLength();
            InputStreamResource resource = new InputStreamResource(photo.getFile());
            String contentType = determineContentType(filename);
            PhotoResponseParts response = new PhotoResponseParts(resource, filename, contentType, size);

            return Either.ofRight(response);
        } catch (Exception e) {
            return Either.ofLeft(OperationError.internalServerError(e.getMessage()));
        }
    }

    /**
     * Get metadata of all photos in the specified album
     *
     * @param albumId The id of the album
     * @return The metadata of the photos
     */
    public Either<String, List<PhotoMetadata>> getMetadataOfPhotosInAlbum(String albumId) {
        Album album = albumService.findById(albumId).orElseThrow(() -> new IllegalArgumentException(NO_SUCH_ALBUM_EXISTS));
        if (!this.permissionsService.isOwnerAdminOrEditorOrViewer(album)) {
            return Either.ofLeft(NO_PERMISSION_TO_VIEW_PHOTO);
        }
        return Either.ofRight(this.photoService.getAllPhotoMetadataInAlbum(albumId));
    }

    /**
     * Get metadata of a photo with the specified id
     *
     * @param photoId The id of the photo
     * @return The metadata of the photo
     */
    public Either<String, PhotoMetadata> getPhotoById(String photoId) {
        Optional<PhotoMetadata> photoMetadata = this.photoService.getPhotoById(photoId);

        if (photoMetadata.isEmpty()) {
            return Either.ofLeft(NO_SUCH_PHOTO_FOUND);
        }

        String albumId = photoMetadata.get().getAlbumId();

        Album album = albumService.findById(albumId).orElseThrow(() -> new IllegalArgumentException(NO_SUCH_ALBUM_EXISTS + ": " + albumId));
        if (!this.permissionsService.isOwnerAdminOrEditorOrViewer(album)) {
            return Either.ofLeft(NO_PERMISSION_TO_VIEW_PHOTO);
        }

        return photoMetadata.map(Either::<String, PhotoMetadata>ofRight).orElse(Either.ofLeft(NO_METADATA_FOUND));
    }

    /**
     * Move a photo to a different album
     *
     * @param photoId       The id of the photo to move
     * @param targetAlbumId The id of the album to move the photo to
     * @return result of the operation
     */
    public Either<OperationError, String> movePhotoToAlbum(String photoId, String targetAlbumId) {
        Optional<PhotoMetadata> photoMetadataOpt = this.photoService.getPhotoById(photoId);
        if (photoMetadataOpt.isEmpty()) {
            return Either.ofLeft(OperationError.notFound(PHOTO_NOT_FOUND));
        }

        String currentAlbumId = photoMetadataOpt.get().getAlbumId();

        if (!this.permissionsService.isCtxUserOwnerOfAlbum(currentAlbumId) || !this.permissionsService.isCtxUserOwnerOfAlbum(targetAlbumId)) {
            return Either.ofLeft(OperationError.forbidden(NO_PERMISSION_TO_MOVE_PHOTO));
        }

        albumService.incrementPhotoCount(targetAlbumId);
        albumService.decrementPhotoCount(currentAlbumId);

        this.photoService.movePhotoToAlbum(photoId, targetAlbumId);
        return Either.ofRight("Photo moved successfully");
    }

    /**
     * Duplicate a photo to a different album
     *
     * @param photoId       The id of the photo to duplicate
     * @param targetAlbumId The id of the album to duplicate the photo to
     * @return result of the operation
     */
    public Either<String, String> duplicatePhotoById(String photoId, String targetAlbumId) {
        Optional<PhotoMetadata> photoMetadataOpt = this.photoService.getPhotoById(photoId);
        if (photoMetadataOpt.isEmpty()) {
            return Either.ofLeft(PHOTO_NOT_FOUND);
        }

        String albumId = photoMetadataOpt.get().getAlbumId();
        Album currentAlbum = albumService.findById(albumId).orElseThrow(() -> new IllegalArgumentException(NO_SUCH_ALBUM_EXISTS));

        if (!this.permissionsService.isOwnerAdminOrEditor(currentAlbum)) {
            return Either.ofLeft(NO_PERMISSION_TO_DUPLICATE_PHOTO);
        }

        Album targetAlbum = albumService.findById(targetAlbumId).orElseThrow(() -> new IllegalArgumentException(NO_SUCH_ALBUM_EXISTS));

        if (!this.permissionsService.isOwnerAdminOrEditor(targetAlbum)) {
            return Either.ofLeft(NO_PERMISSION_TO_DUPLICATE_PHOTO + " to this album");
        }

        try {
            User user = this.permissionsService.getUserFromCtx();
            String newPhotoId = this.photoService.duplicatePhoto(photoId, targetAlbumId, user.getId());
            this.albumService.incrementPhotoCount(targetAlbumId);
            return Either.ofRight("Photo duplicated successfully with ID: " + newPhotoId);
        } catch (Exception e) {
            return Either.ofLeft("Failed to duplicate photo: " + e.getMessage());
        }
    }

    /**
     * Get metadata of a photo with the specified id
     *
     * @param photoId The id of the photo
     * @return The metadata of the photo
     */
    public Either<String, PhotoMetadata> getPhotoMetadataById(String photoId) {
        Optional<PhotoMetadata> photo = this.photoService.getPhotoById(photoId);

        if (photo.isEmpty()) {
            return Either.ofLeft(PHOTO_NOT_FOUND);
        }

        String albumId = photo.get().getAlbumId();
        Album album = albumService.findById(albumId).orElseThrow(() -> new IllegalArgumentException(NO_SUCH_ALBUM_EXISTS));

        // Check if the user is the owner of the album or has permission ADMINISTRATOR or EDITOR
        if (!this.permissionsService.isOwnerAdminOrEditorOrViewer(album)) {
            return Either.ofLeft(NO_PERMISSION_TO_VIEW_PHOTO);
        }

        PhotoMetadata metadata = this.photoService.getPhotoById(photoId).orElseThrow(() -> new IllegalArgumentException(NO_METADATA_FOUND));
        return Either.ofRight(metadata);
    }

    /**
     * Get metadata of all photos with the specified tag
     *
     * @param tag The tag to search for
     * @return The metadata of the photos
     */
    public Either<String, List<PhotoMetadata>> getPhotosWithTag(String tag) {
        User user = this.permissionsService.getUserFromCtx();
        return Either.ofRight(this.photoService.getPhotosWithTag(user.getId(), tag));
    }

    /**
     * Get metadata of all photos with the specified tag in the specified album
     *
     * @param tag     The tag to search for
     * @param albumId The id of the album
     * @return The metadata of the photos
     */
    public Either<String, List<PhotoMetadata>> getPhotosWithTagInAlbum(String tag, String albumId) {
        Album album = albumService.findById(albumId).orElseThrow(() -> new IllegalArgumentException(NO_SUCH_ALBUM_EXISTS));
        User user = this.permissionsService.getUserFromCtx();
        if (!permissionsService.isOwnerAdminOrEditor(album)) {
            return Either.ofLeft(NO_PERMISSION_TO_VIEW_PHOTO);
        }
        return Either.ofRight(this.photoService.getPhotosWithTagInAlbum(user.getId(), tag, album));
    }

    /**
     * Get all tags on photos belonging to the user
     *
     * @return The tags
     */
    public Either<String, List<String>> getAllTagsForUser() {
        User user = this.permissionsService.getUserFromCtx();
        return Either.ofRight(photoService.getAllTagsForUser(user.getId()));
    }

    /**
     * Get all tags on photos in the specified album
     *
     * @param albumId The id of the album
     * @return The tags
     */
    public Either<String, List<String>> getAllTagsForUserInAlbum(String albumId) {
        Album album = albumService.findById(albumId).orElseThrow(() -> new IllegalArgumentException(NO_SUCH_ALBUM_EXISTS));
        User user = this.permissionsService.getUserFromCtx();
        if (!this.permissionsService.isUserFromCtxOwnerOfAlbum(album)) {
            return Either.ofLeft(NO_ACCESS_TO_SUCH_ALBUM);
        }
        Either<String, List<String>> result = this.photoService.getAllTagsForUserInAlbum(user.getId(), album);
        if (result.isLeft()) {
            return Either.ofLeft(result.left().orElseThrow(() -> new RuntimeException("No tags found")));
        } else {
            return Either.ofRight(result.right().orElseThrow(() -> new RuntimeException("No tags found")));
        }
    }

    /**
     * Create a new album with the specified name
     *
     * @param parentAlbumId The id of the parent album
     * @param newAlbumName  The name of the new album
     * @return The new album
     */
    public Either<String, Album> createAlbum(@NotBlank String parentAlbumId, @NotBlank String newAlbumName) {
        Optional<Album> parentAlbumOpt = albumService.findById(parentAlbumId);
        if (parentAlbumOpt.isEmpty()) {
            return Either.ofLeft(NO_SUCH_ALBUM_EXISTS);
        }

        if (!permissionsService.isUserFromCtxOwnerOrAdmin(parentAlbumOpt.get())) {
            return Either.ofLeft(NO_ACCESS_TO_SUCH_ALBUM);
        }

        return albumService.createAlbum(parentAlbumId, newAlbumName);
    }

    /**
     * Delete the album with the specified id
     *
     * @param albumId                  The id of the album to delete
     * @param moveChildrenDataToParent Whether to move children albums to the parent album
     * @param moveToParentAlbum        Whether to move in the current album to the parent album or delete them
     * @return result of the operation
     */
    public Either<OperationError, ?> deleteById(String albumId, boolean moveChildrenDataToParent, boolean moveToParentAlbum) {
        // The album cannot be deleted if it is the root album
        Optional<Album> albumToBeDeletedOpt = albumService.findById(albumId);
        if (albumToBeDeletedOpt.isEmpty()) {
            return Either.ofLeft(OperationError.notFound(NO_SUCH_ALBUM_EXISTS));
        }
        Album albumToBeDeleted = albumToBeDeletedOpt.get();

        // Check if user is owner of the album
        if (!permissionsService.isUserFromCtxOwnerOfAlbum(albumToBeDeleted.getOwnerId())) {
            return Either.ofLeft(OperationError.forbidden(NO_ACCESS_TO_SUCH_ALBUM));
        }


        if (albumToBeDeleted.isRoot()) {
            return Either.ofLeft(OperationError.badRequest(ROOT_ALBUM_CANNOT_BE_DELETED));
        }

        // This should not return empty list but an error message
        List<Album> subAlbums = albumService.getChildren(albumId).rightOrElse(Collections.emptyList());

        if (moveChildrenDataToParent) {
            // Get parent of the current album and assign it as the parent of those children albums
            albumService.findById(albumToBeDeleted.getParentId()).orElseThrow(() -> new RuntimeException("Album to be deleted had no parent"));
            for (Album album : subAlbums) {
                albumService.changeParent(album.getId(), albumToBeDeleted.getParentId());
                photoService.movePhotosToAlbum(album.getId(), albumToBeDeleted.getParentId());
            }
        } else {
            // Delete the albums and the photos in them
            for (Album album : subAlbums) {
                photoService.deleteAllPhotosInAlbum(album.getId());
                albumService.deleteByIdNoChecks(album.getId());
            }
        }

        if (moveToParentAlbum) {
            // Move photos in this album to the parent album
            photoService.movePhotosToAlbum(albumId, albumToBeDeleted.getParentId());
            albumService.incrementPhotoCountBy(albumToBeDeleted.getParentId(), albumToBeDeleted.getPhotoCount());

        } else {
            // Delete photos in this album
            photoService.deleteAllPhotosInAlbum(albumId);
        }

        // Delete this album
        albumService.deleteByIdNoChecks(albumToBeDeleted.getId());

        return Either.ofRight("albums deleted");
    }

    /**
     * Add user access to the album with the specified id
     *
     * @param albumId The id of the album
     * @param userId  The id of the user to add
     * @param role    The role to assign to the user
     * @return result of the operation
     */
    public Either<String, String> addAccessToAlbum(String albumId, String userId, AlbumAccessRoles role) {
        // TODO: Why this requires a username and not an id?
        Optional<Album> album = albumService.findById(albumId);
        if (album.isEmpty()) {
            return Either.ofLeft(NO_SUCH_ALBUM_EXISTS);
        }

        if (!permissionsService.isUserFromCtxOwnerOrAdmin(album.get())) {
            return Either.ofLeft(NO_ACCESS_TO_SUCH_ALBUM);
        }

        Either<String, String> canUserGetAccessToAlbum = permissionsService.canUserGetAccessToAlbum(userId, album.get());
        if (canUserGetAccessToAlbum.isLeft()) {
            return Either.ofLeft(canUserGetAccessToAlbum.leftOrElse("Internal error"));
        }

        return albumService.addAccessToAlbum(albumId, userId, role);
    }

    /**
     * Get permissions for the album with the specified id
     *
     * @param albumId The id of the album
     * @return The permissions
     */
    public Either<String, ?> getPermissions(String albumId) {
        Optional<Album> album = albumService.findById(albumId);
        if (album.isEmpty()) {
            return Either.ofLeft(NO_SUCH_ALBUM_EXISTS);
        } else if (permissionsService.isUserFromCtxOwnerOfAlbum(album.get())) {
            return Either.ofLeft(NO_ACCESS_TO_SUCH_ALBUM);
        }

        return albumService.getPermissions(albumId);
    }

    /**
     * Edit access to the album with the specified id
     *
     * @param albumId The id of the album
     * @param userId  The id of the user
     * @param role    The new role
     * @return result of the operation
     */
    public Either<String, String> editAccessToAlbum(String albumId, String userId, AlbumAccessRoles role) {
        Optional<Album> album = albumService.findById(albumId);
        if (album.isEmpty()) {
            return Either.ofLeft(NO_SUCH_ALBUM_EXISTS);
        }

        if (!permissionsService.isUserFromCtxOwnerOfAlbum(album.get())) {
            return Either.ofLeft(NO_ACCESS_TO_SUCH_ALBUM);
        }

        return albumService.editAccessToAlbum(albumId, userId, role);
    }

    /**
     * Remove user access to the album with the specified id
     *
     * @param albumId The id of the album
     * @param userId  The id of the user to remove
     * @return result of the operation
     */
    public Either<String, String> removeAccessToAlbum(String albumId, String userId) {
        User currentUser = permissionsService.getUserFromCtx();
        Optional<Album> albumOpt = albumService.findById(albumId);

        if (albumOpt.isEmpty()) {
            return Either.ofLeft(NO_SUCH_ALBUM_EXISTS);
        }
        if (!permissionsService.isUserOwnerOrAdmin(albumOpt.get(), currentUser.getId())) {
            return Either.ofLeft(NO_ACCESS_TO_SUCH_ALBUM);
        }

        return albumService.removeAccessToAlbum(albumId, userId);
    }

    /**
     * Get all children of the album with the specified id
     *
     * @param albumId The id of the album
     * @return The children of the album
     */
    public Either<String, List<Album>> getChildrenOfAlbum(String albumId) {
        Optional<Album> album = albumService.findById(albumId);
        if (album.isEmpty()) return Either.ofLeft(NO_SUCH_ALBUM_EXISTS);
        if (!permissionsService.isUserFromCtxOwnerOfAlbum(album.get())) {
            return Either.ofLeft(NO_ACCESS_TO_SUCH_ALBUM);
        }

        return albumService.getChildren(albumId);
    }

    /**
     * Get all albums owned by the user who authenticated the request
     *
     * @return The albums
     */
    public List<Album> getAllAlbumsOwnedByUser() {
        User user = permissionsService.getUserFromCtx();
        return albumService.getAllAlbumsOwnedByUser(user.getId());
    }

    /**
     * Get album with the specified id
     *
     * @param albumId The id of the album
     * @return The album
     */
    public Either<OperationError, Album> getAlbumById(String albumId) {
        Optional<Album> albumOpt = albumService.findById(albumId);
        if (albumOpt.isEmpty()) {
            return Either.ofLeft(OperationError.notFound(NO_SUCH_ALBUM_EXISTS));
        }
        Album album = albumOpt.get();
        if (!permissionsService.isOwnerAdminOrEditorOrViewer(album)) {
            return Either.ofLeft(OperationError.forbidden(NO_ACCESS_TO_SUCH_ALBUM));
        }
        return Either.ofRight(album);
    }

    /**
     * Get the root album for the user who authenticated the request
     *
     * @return The root album
     */
    public Either<OperationError, Album> getRootAlbum() {
        User user = permissionsService.getUserFromCtx();
        Album album = albumService.getRootAlbum(user.getId());
        if (album.getOwnerId().equals(user.getId())) {
            return Either.ofRight(album);
        }
        return Either.ofLeft(OperationError.forbidden(NO_ACCESS_TO_SUCH_ALBUM));
    }

    /**
     * Get all ancestors of the album with the specified id
     *
     * @param albumId The id of the album
     * @return The ancestors of the album
     */
    public Either<String, List<Album>> getAncestorAlbums(String albumId) {
        // TODO: Permissions check
        return Either.ofRight(albumService.getAncestorAlbums(albumId));
    }

    /**
     * Get information about users who have access to the album with the specified id
     *
     * @param albumId The id of the album
     * @return The information about the users
     */
    public Either<OperationError, List<SharedUsersInfo>> getSharedUsers(String albumId) {
        Optional<Album> albumOpt = albumService.findById(albumId);
        if (albumOpt.isEmpty()) {
            return Either.ofLeft(OperationError.notFound(NO_SUCH_ALBUM_EXISTS));
        }

        Album album = albumOpt.get();
        if (!permissionsService.isOwnerAdminOrEditorOrViewer(album)) {
            return Either.ofLeft(OperationError.forbidden(NO_ACCESS_TO_SUCH_ALBUM));
        }

        // Get the names of users whose ids are stored in the permissions of the album
        Map<String, String> permissions = album.getPermissions();

        // Get all users by id
        List<SharedUsersInfo> sharedUsers = new ArrayList<>();
        for (Map.Entry<String, String> entry : permissions.entrySet()) {
            User sharedUser = permissionsService.getUserById(entry.getKey());
            sharedUsers.add(new SharedUsersInfo(sharedUser.getId(), sharedUser.getUsername(), entry.getValue()));
        }

        return Either.ofRight(sharedUsers);
    }

    /**
     * Get information about all shared albums for the user who authenticated the request
     *
     * @return The information about the shared albums
     */
    public Either<OperationError, List<SharedAlbumInformation>> getSharedAlbums() {
        return Either.ofRight(albumService.getSharedAlbumsForUser(permissionsService.getUserFromCtx().getId()));
    }

    /**
     * Return information about album from a shared perspective
     *
     * @param albumId The id of the album
     * @return The information about the album
     */
    public Either<OperationError, SharedAlbumInformation> getInformationAboutSharedAlbum(String albumId) {
        Optional<Album> albumOpt = albumService.findById(albumId);
        if (albumOpt.isEmpty()) {
            return Either.ofLeft(OperationError.notFound(NO_SUCH_ALBUM_EXISTS));
        }

        Album album = albumOpt.get();
        if (!permissionsService.isOwnerAdminOrEditorOrViewer(album)) {
            return Either.ofLeft(OperationError.forbidden(NO_ACCESS_TO_SUCH_ALBUM));
        }

        return Either.ofRight(new SharedAlbumInformation(album.getId(), album.getName(), album.getPermissions().get(permissionsService.getUserFromCtx().getId()), album.getOwnerId(), album.getCreatedAt(), album.getThumbnailId()));
    }

    /**
     * Get all albums for the user who authenticated the request
     *
     * @return The albums
     */
    public Either<OperationError, List<Album>> getAllAlbums() {
        User user = permissionsService.getUserFromCtx();
        return Either.ofRight(albumService.getAllAlbumsOwnedOrSharedWithUser(user.getId()));
    }

    /**
     * Delete the photo with the specified id
     *
     * @param photoId The id of the photo
     * @return The result of the operation
     */
    public Either<OperationError, String> deletePhotoById(String photoId) {
        Optional<PhotoMetadata> photoMetadataOpt = this.photoService.getPhotoById(photoId);

        if (photoMetadataOpt.isEmpty()) {
            return Either.ofLeft(OperationError.notFound(PHOTO_NOT_FOUND));
        }

        String albumId = photoMetadataOpt.get().getAlbumId();
        Album album = albumService.findById(albumId).orElseThrow(() -> new IllegalArgumentException(NO_SUCH_ALBUM_EXISTS));

        if (!this.permissionsService.isUserFromCtxOwnerOfAlbum(album)) {
            return Either.ofLeft(OperationError.forbidden(NO_PERMISSION_TO_DELETE_PHOTO));
        }

        this.photoService.deletePhotoById(photoId);
        this.albumService.decrementPhotoCount(albumId);
        logger.info("Photo with id " + photoId + " deleted");
        return Either.ofRight("Photo deleted successfully");
    }

    /**
     * Get the public information about an album hiding the implementation details
     *
     * @param albumId The id of the album
     * @return The public information about the album
     */
    public Either<OperationError, AlbumInformation> getPublicAlbumInformation(String albumId) {
        // This information can be accessed by user with any kind of access (owner or shared)
        Optional<Album> albumOpt = albumService.findById(albumId);
        if (albumOpt.isEmpty()) {
            return Either.ofLeft(OperationError.notFound(NO_SUCH_ALBUM_EXISTS));
        }

        Album album = albumOpt.get();

        if (!permissionsService.isOwnerAdminOrEditorOrViewer(album)) {
            return Either.ofLeft(OperationError.forbidden(NO_ACCESS_TO_SUCH_ALBUM));
        }

        // Get usernames of users who have access to the album
        // TODO: Make this into a list, without the role
        Map<String, String> usernamesWithAccess = new HashMap<>();
        for (Map.Entry<String, String> entry : album.getPermissions().entrySet()) {
            User sharedUser = permissionsService.getUserById(entry.getKey());
            usernamesWithAccess.put(sharedUser.getUsername(), entry.getValue());
        }

        // Get username of the owner
        User owner = permissionsService.getUserById(album.getOwnerId());

        AlbumInformation albumInformation = new AlbumInformation(album.getName(), owner.getUsername(), album.getCreatedAt(), usernamesWithAccess);

        return Either.ofRight(albumInformation);
    }

    /**
     * Update the tags of a photo
     *
     * @param photoId The id of the photo
     * @param tags    The new tags
     * @return The result of the operation
     */
    public Either<OperationError, String> updateTags(String photoId, Set<String> tags) {
        Optional<PhotoMetadata> photoMetadataOpt = this.photoService.getPhotoById(photoId);
        String albumId = photoMetadataOpt.orElseThrow(() -> new IllegalArgumentException(PHOTO_NOT_FOUND)).getAlbumId();
        Album album = albumService.findById(albumId).orElseThrow(() -> new IllegalArgumentException(NO_SUCH_ALBUM_EXISTS));
        if (!this.permissionsService.isUserFromCtxOwnerOfAlbum(album)) {
            return Either.ofLeft(OperationError.forbidden(PHOTO_NOT_FOUND));
        }

        this.photoService.editTags(photoId, tags);
        return Either.ofRight("Tags updated successfully");
    }

    /**
     * Get the number of albums in the system
     *
     * @return The number of albums
     */
    public long getNumberOfAlbums() {
        return albumService.getNumberOfAlbums();
    }

    /**
     * Get the number of photos in the system
     *
     * @return The number of photos
     */
    public long getNumberOfPhotos() {
        return photoService.getNumberOfPhotos();
    }

    /**
     * Get the names and sizes of all albums owned by the user
     *
     * @param user The user
     * @return The names and sizes of the albums
     */
    public Map<String, Long> getAlbumNamesAndSizes(User user) {
        Map<String, Long> albumNamesAndSizes = new HashMap<>();
        List<Album> albums = albumService.getAllAlbumsOwnedByUser(user.getId());
        for (Album album : albums) {
            albumNamesAndSizes.put(album.getName(), photoService.getSpaceUsedByAlbum(album.getId()));
        }
        return albumNamesAndSizes;
    }

    /**
     * Get the total space used in the system
     *
     * @return The space used
     */
    public long getSpaceUsedInSystem() {
        return photoService.getSpaceUsed();
    }

    /**
     * Delete all photos and albums owned by the user with the specified id
     *
     * @param userId The id of the user
     */
    public void deleteUserPhotosAndAlbums(String userId) {
        photoService.deletePhotosByUser(userId);
        albumService.deleteAllAlbumsOwnedByUser(userId);
    }

    /**
     * Get metadata of all photos with the specified tag grouped by albums
     *
     * @param tag The tag to search for
     * @return The metadata of the photos
     */
    public List<PhotoGroupedByAlbum> getPhotoMetadataByTagGroupedByAlbums(String tag) {
        User user = this.permissionsService.getUserFromCtx();
        if (!permissionsService.isContextUser(user.getId())) {
            return new ArrayList<>();
        }
        return photoService.getPhotoMetadataByTagGroupedByAlbums(tag, user.getId());
    }

    /**
     * Set the photo with the specified id as the album cover
     *
     * @param albumId The id of the album
     * @param photoId The id of the photo
     * @return The result of the operation
     */
    public Either<OperationError, String> setPhotoAsAlbumCover(String albumId, String photoId) {
        Optional<Album> albumOpt = albumService.findById(albumId);
        if (albumOpt.isEmpty()) {
            return Either.ofLeft(OperationError.notFound(NO_SUCH_ALBUM_EXISTS));
        }

        Album album = albumOpt.get();
        if (!permissionsService.isUserFromCtxOwnerOrAdmin(album)) {
            return Either.ofLeft(OperationError.forbidden(NO_ACCESS_TO_SUCH_ALBUM));
        }

        // Find thumbnailId of photo
        Optional<PhotoMetadata> photoMetadataOpt = photoService.getPhotoById(photoId);
        if (photoMetadataOpt.isEmpty()) {
            return Either.ofLeft(OperationError.notFound(PHOTO_NOT_FOUND));
        }

        return albumService.setPhotoAsAlbumCover(albumId, photoId);
    }

    /**
     * Remove the album cover photo from the album
     *
     * @param albumId The id of the album
     * @return The result of the operation
     */
    public Either<OperationError, String> removePhotoAsAlbumCover(String albumId) {
        Optional<Album> albumOpt = albumService.findById(albumId);
        if (albumOpt.isEmpty()) {
            return Either.ofLeft(OperationError.notFound(NO_SUCH_ALBUM_EXISTS));
        }

        Album album = albumOpt.get();

        if (!permissionsService.isUserFromCtxOwnerOrAdmin(album)) {
            return Either.ofLeft(OperationError.forbidden(NO_ACCESS_TO_SUCH_ALBUM));
        }

        return albumService.removePhotoAsAlbumCover(albumId);
    }

    /**
     * Rename an album
     *
     * @param albumId The id of the album
     * @param newName The new name of the album
     * @return The result of the operation
     */
    public Either<OperationError, String> renameAlbum(String albumId, String newName) {
        // Only owner of the album can rename it
        Optional<Album> albumOpt = albumService.findById(albumId);
        if (albumOpt.isEmpty()) {
            return Either.ofLeft(OperationError.notFound(NO_SUCH_ALBUM_EXISTS));
        }

        Album album = albumOpt.get();
        if (!permissionsService.isUserFromCtxOwnerOfAlbum(album)) {
            return Either.ofLeft(OperationError.forbidden(NO_ACCESS_TO_SUCH_ALBUM));
        }

        return albumService.renameAlbum(albumId, newName);
    }

    /**
     * Search for users by username
     *
     * @param query The query to search for
     * @return The users
     */
    public List<User> searchUsers(String query) {
        return this.permissionsService.searchUsers(query);
    }

    private String determineContentType(String filename) {
        String fileExtension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (fileExtension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }

    private boolean photoIdIsInvalid(String id) {
        if (id == null || id.isEmpty()) {
            return true;
        }
        return !ObjectId.isValid(id);
    }

}
