package dev.kapiaszczyk.mpp.repositories;

import dev.kapiaszczyk.mpp.models.database.PhotoMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Interface representing operations performed on photos.
 */
public interface PhotoRepository extends MongoRepository<PhotoMetadata, String> {

}
