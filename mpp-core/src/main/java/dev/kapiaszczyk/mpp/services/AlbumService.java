package dev.kapiaszczyk.mpp.services;

import dev.kapiaszczyk.mpp.constants.Constants;
import dev.kapiaszczyk.mpp.errors.OperationError;
import dev.kapiaszczyk.mpp.models.AlbumAccessRoles;
import dev.kapiaszczyk.mpp.models.api.SharedAlbumInformation;
import dev.kapiaszczyk.mpp.models.database.Album;
import dev.kapiaszczyk.mpp.models.database.User;
import dev.kapiaszczyk.mpp.repositories.AlbumRepository;
import dev.kapiaszczyk.mpp.util.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static dev.kapiaszczyk.mpp.errors.GenericErrors.*;
import static dev.kapiaszczyk.mpp.models.api.SharedAlbumInformation.mapAlbumToSharedAlbumInfo;
import static dev.kapiaszczyk.mpp.models.database.Album.createFromParent;

/**
 * Service holding logic for album operations, such as creating, deleting, renaming and managing permissions
 * for albums.
 */
@Service
public class AlbumService {

    @Autowired
    private final MongoTemplate mongoTemplate;

    @Autowired
    private final AlbumRepository albumRepository;

    public AlbumService(AlbumRepository albumRepository, MongoTemplate mongoTemplate) {
        this.albumRepository = albumRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Method to create a root album for a user when they register
     * <p>
     * The name is guaranteed to be unique as it is the same as the username,
     * for which uniqueness is enforced.
     */
    public void createRootAlbumForUser(User user) {
        Album album = new Album(
                user.getUsername(),
                "",
                ("/" + user.getUsername()),
                true,
                user.getId(),
                Date.from(Instant.now())
        );

        albumRepository.save(album);
    }

    /**
     * Create a new album for a user.
     *
     * @param parentAlbumId id of the parent album
     * @param name          name of the new album
     * @return result of the operation containing the created album or error message
     */
    public Either<String, Album> createAlbum(String parentAlbumId, String name) {
        return albumRepository.findById(parentAlbumId)
                .<Either<String, Album>>map(parentAlbum -> {
                    if (doesAlbumExist(name, parentAlbum.getId())) {
                        return Either.ofLeft(ALBUM_WITH_THE_SAME_NAME_ALREADY_EXISTS);
                    }

                    if (!parentAlbum.isRoot() && !Constants.NESTING_ALBUMS_ALLOWED) {
                        return Either.ofLeft(NESTED_ALBUMS_ARE_NOT_ENABLED);
                    }

                    Album album = createFromParent(name, parentAlbum);
                    albumRepository.save(album);
                    return Either.ofRight(album);
                })
                .orElseGet(() -> Either.ofLeft(PARENT_ALBUM_NOT_FOUND));
    }

    /**
     * Add access permissions for a user to a shared album.
     *
     * @param albumId id of the album
     * @param userId  id of the user
     * @param role    role for the user
     * @return result of the operation containing success message or error message
     */
    public Either<String, String> addAccessToAlbum(String albumId, String userId, AlbumAccessRoles role) {
        return albumRepository.findById(albumId)
                .<Either<String, String>>map(album -> {
                    if (album.getPermissions().containsKey(userId)) {
                        return Either.ofLeft(USER_ALREADY_HAS_ACCESS);
                    }
                    album.getPermissions().put(userId, role.name());
                    albumRepository.save(album);
                    return Either.ofRight(PERMISSIONS_ADDED);
                })
                .orElse(Either.ofLeft(NO_SUCH_ALBUM_EXISTS));
    }

    /**
     * Edit access permissions for a user on a shared album.
     *
     * @param albumId id of the album
     * @param userId  id of the user
     * @param role    new role for the user
     * @return result of the operation containing success message or error message
     */
    public Either<String, String> editAccessToAlbum(String albumId, String userId, AlbumAccessRoles role) {
        return albumRepository.findById(albumId)
                .<Either<String, String>>map(album -> {
                    if (!album.getPermissions().containsKey(userId)) {
                        return Either.ofLeft(NO_SUCH_USER);
                    } else if (album.getPermissions().get(userId).equals(role.name())) {
                        return Either.ofLeft(ROLE_ALREADY_SET);
                    }
                    album.getPermissions().put(userId, role.name());
                    albumRepository.save(album);
                    return Either.ofRight(PERMISSION_MODIFIED);
                })
                .orElse(Either.ofLeft(NO_SUCH_ALBUM_EXISTS));
    }

    /**
     * Remove access to a shared album for a user.
     *
     * @param albumId id of the album
     * @param userId  id of the user
     * @return result of the operation containing success message or error message
     */
    public Either<String, String> removeAccessToAlbum(String albumId, String userId) {
        return albumRepository.findById(albumId)
                .<Either<String, String>>map(album -> {
                    if (!album.getPermissions().containsKey(userId)) {
                        return Either.ofLeft(NO_SUCH_USER);
                    }
                    album.getPermissions().remove(userId);
                    albumRepository.save(album);
                    return Either.ofRight(PERMISSIONS_REMOVED);
                })
                .orElse(Either.ofLeft(NO_SUCH_ALBUM_EXISTS));
    }

    /**
     * Get permissions assigned for users on an album.
     *
     * @param albumId id of the album
     * @return result of the operation containing permissions or error message
     */
    public Either<String, Map<String, String>> getPermissions(String albumId) {
        return albumRepository.findById(albumId)
                .<Either<String, Map<String, String>>>map(value -> Either.ofRight(value.getPermissions())).orElseGet(() -> Either.ofLeft(NO_SUCH_ALBUM_EXISTS));
    }

    /**
     * Get all children of an album.
     *
     * @param albumId id of the album
     * @return list of children
     */
    public Either<String, List<Album>> getChildren(String albumId) {
        return albumRepository.findById(albumId)
                .<Either<String, List<Album>>>map(value -> Either.ofRight(albumRepository.findAllByParentId(value.getId())))
                .orElseGet(() -> Either.ofLeft(NO_SUCH_ALBUM_EXISTS));
    }

    /**
     * Get album by id.
     *
     * @param id id of the album
     * @return album
     */
    public Optional<Album> findById(String id) {
        return albumRepository.findById(id);
    }

    /**
     * Get all albums belonging to a user.
     *
     * @param userId id of the user
     * @return list of albums
     */
    public List<Album> getAllAlbumsOwnedByUser(String userId) {
        return albumRepository.findAllByOwnerId(userId);
    }

    /**
     * Get root album for a user.
     *
     * @param userId id of the user
     * @return root album
     */
    public Album getRootAlbum(String userId) {
        return albumRepository
                .findByOwnerIdAndParentId(userId, "")
                .orElseThrow(() -> new IllegalArgumentException("Root album not found"));
    }

    /**
     * Get all ancestor albums that are higher in the hierarchy.
     *
     * @param albumId id of the album
     * @return list of ancestors
     */
    public List<Album> getAncestorAlbums(String albumId) {
        return albumRepository.findById(albumId)
                .map(album -> albumRepository.findAllByNameIn(album.getNamesOfAlbumsFromPath()))
                .orElse(Collections.emptyList());
    }

    /**
     * Change parent of an album.
     *
     * @param albumId id of the album
     */
    public void changeParent(String albumId, String newParentId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Album not found: " + albumId));

        Album newParentAlbum = albumRepository.findById(newParentId)
                .orElseThrow(() -> new IllegalArgumentException("New parent album not found: " + newParentId));

        album.setParentId(newParentId);
        album.setPath(newParentAlbum.getPath() + "/" + album.getName());
        albumRepository.save(album);
    }

    /**
     * Get shared albums for a user.
     *
     * @param userId id of the user
     * @return list of shared albums
     */
    public List<SharedAlbumInformation> getSharedAlbumsForUser(String userId) {
        List<Album> albums = albumRepository.findByUserIdInPermissions(userId);
        return albums.stream()
                .map(album -> mapAlbumToSharedAlbumInfo(album, userId))
                .collect(Collectors.toList());
    }

    /**
     * Get all albums owned by a user or shared with the user.
     *
     * @param userId id of the user
     * @return list of albums owned by the user or shared with the user
     */
    public List<Album> getAllAlbumsOwnedOrSharedWithUser(String userId) {
        return albumRepository.findAllByOwnerIdOrUserIdInPermissions(userId);
    }

    /**
     * Get number of albums in the database.
     *
     * @return number of albums
     */
    public long getNumberOfAlbums() {
        return albumRepository.count();
    }

    /**
     * Delete all albums owned by a user.
     *
     * @param userId id of the user
     */
    public void deleteAllAlbumsOwnedByUser(String userId) {
        albumRepository.deleteAllByOwnerId(userId);
    }

    /**
     * Set photo as album cover.
     *
     * @param albumId id of the album
     * @param photoId id of the photo
     * @return result of the operation
     */
    public Either<OperationError, String> setPhotoAsAlbumCover(String albumId, String photoId) {
        Optional<Album> album = albumRepository.findById(albumId);
        if (album.isEmpty()) {
            return Either.ofLeft(OperationError.notFound(NO_SUCH_ALBUM_EXISTS));
        }
        album.get().setThumbnailId(photoId);
        albumRepository.save(album.get());
        return Either.ofRight("Photo set as album cover");
    }

    /**
     * Remove photo as album cover.
     *
     * @param albumId id of the album
     * @return result of the operation
     */
    public Either<OperationError, String> removePhotoAsAlbumCover(String albumId) {
        Optional<Album> album = albumRepository.findById(albumId);
        if (album.isPresent()) {
            album.get().setThumbnailId(null);
            albumRepository.save(album.get());
            return Either.ofRight("Photo removed as album cover");
        } else {
            return Either.ofLeft(OperationError.notFound(NO_SUCH_ALBUM_EXISTS));
        }
    }

    /**
     * Rename an album.
     *
     * @param albumId id of the album to rename
     * @param newName new name for the album
     * @return result of the operation
     */
    public Either<OperationError, String> renameAlbum(String albumId, String newName) {
        return albumRepository.findById(albumId)
                .<Either<OperationError, String>>map(album -> {
                    album.renameAlbum(newName);
                    albumRepository.save(album);
                    return Either.ofRight("Album renamed successfully");
                })
                .orElseGet(() -> Either.ofLeft(OperationError.notFound(NO_SUCH_ALBUM_EXISTS)));
    }

    // TODO: This is bad, refactor and remove this method
    public void deleteByIdNoChecks(String albumId) {
        albumRepository.deleteById(albumId);
    }

    protected void incrementPhotoCount(String albumId) {
        Update update = new Update().inc("photoCount", 1);
        Query query = new Query(Criteria.where("_id").is(albumId));

        mongoTemplate.updateFirst(query, update, Album.class);
    }

    protected void decrementPhotoCount(String albumId) {
        Update update = new Update().inc("photoCount", -1);
        Query query = new Query(Criteria.where("_id").is(albumId));

        mongoTemplate.updateFirst(query, update, Album.class);
    }

    protected void incrementPhotoCountBy(String albumId, long count) {
        Update update = new Update().inc("photoCount", count);
        Query query = new Query(Criteria.where("_id").is(albumId));

        mongoTemplate.updateFirst(query, update, Album.class);
    }

    protected void decrementPhotoCountBy(String albumId, long count) {
        Update update = new Update().inc("photoCount", -count);
        Query query = new Query(Criteria.where("_id").is(albumId));

        mongoTemplate.updateFirst(query, update, Album.class);
    }

    /**
     * Check if album with the given name and parent exist.
     *
     * @param name          name of the album
     * @param parentAlbumId id of the parent album
     * @return true if album exists, false otherwise
     */
    private boolean doesAlbumExist(String name, String parentAlbumId) {
        return albumRepository.findByNameAndParentId(name, parentAlbumId).isPresent();
    }
}
