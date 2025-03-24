package dev.kapiaszczyk.mpp.repositories;

import dev.kapiaszczyk.mpp.models.database.Album;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Interface representing operations performed on albums.
 */
@Repository
public interface AlbumRepository extends MongoRepository<Album, String> {

    Album save(Album album);

    List<Album> findAllByOwnerId(String ownerId);

    Optional<Album> findByName(String name);

    Optional<Album> findByNameAndParentId(String name, String parentAlbumName);

    void deleteByPath(String path);

    List<Album> findAllByParentId(String id);

    Optional<Album> findByOwnerIdAndParentId(String userId, String parentId);

    List<Album> findAllByNameIn(String[] pathParts);

    @Query("{'permissions.?0': { $exists: true }}")
    List<Album> findByUserIdInPermissions(String userId);

    @Query("{ $or: [ { 'ownerId': ?0 }, { 'permissions.?0': { $exists: true } } ] }")
    List<Album> findAllByOwnerIdOrUserIdInPermissions(String id);

    @Query("{ 'ownerId': ?0 }")
    void deleteAllByOwnerId(String userId);

    @Query(value = "{ '_id': ?1, 'ownerId': ?0 }", exists = true)
    boolean isUserOwnerOfAlbum(String userId, String albumId);
}
