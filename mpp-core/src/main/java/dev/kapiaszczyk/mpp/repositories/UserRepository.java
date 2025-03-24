package dev.kapiaszczyk.mpp.repositories;

import dev.kapiaszczyk.mpp.models.database.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Interface representing operations performed on users.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);

    User save(User user);

    void delete(User user);

    Optional<User> findByUsername(String username);

    @Query("{ 'username': { $regex: ?0, $options: 'i' } }")
    List<User> findByUsernameRegex(String regex);
}
