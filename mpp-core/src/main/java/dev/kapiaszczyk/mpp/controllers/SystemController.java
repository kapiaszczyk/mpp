package dev.kapiaszczyk.mpp.controllers;

import dev.kapiaszczyk.mpp.endpoints.SystemEndpoints;
import dev.kapiaszczyk.mpp.models.api.UserStatistics;
import dev.kapiaszczyk.mpp.models.database.User;
import dev.kapiaszczyk.mpp.services.CompositeService;
import dev.kapiaszczyk.mpp.services.PermissionsService;
import dev.kapiaszczyk.mpp.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller for handling operations related to admin endpoints.
 */
@RestController
public class SystemController implements SystemEndpoints {

    private final UserService userService;

    private final CompositeService compositeService;

    private final PermissionsService permissionsService;

    public SystemController(UserService userService, CompositeService compositeService, PermissionsService permissionsService) {
        this.userService = userService;
        this.compositeService = compositeService;
        this.permissionsService = permissionsService;
    }

    /**
     * Get all users in the system.
     *
     * @return all users in the system
     */
    @GetMapping("/admin/users")
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.ok(userService.getSharedUsersInfo());
    }

    /**
     * Get all roles in the system.
     *
     * @return all roles in the system
     */
    @GetMapping("/admin/roles")
    public ResponseEntity<?> getRoles() {
        return ResponseEntity.ok(userService.getRoles());
    }

    /**
     * Get the amount of users in the system.
     *
     * @return the amount of users in the system
     */
    @GetMapping("/admin/statistics/users")
    public ResponseEntity<?> getAmountOfUsers() {
        return ResponseEntity.ok(userService.getNumberOfUsers());
    }

    /**
     * Get the amount of photos in the system.
     *
     * @return the amount of photos in the system
     */
    @GetMapping("/admin/statistics/photos")
    public ResponseEntity<?> getAmountOfPhotos() {
        return ResponseEntity.ok(compositeService.getNumberOfPhotos());
    }

    /**
     * Get the amount of albums in the system.
     *
     * @return the amount of albums in the system
     */
    @GetMapping("/admin/statistics/albums")
    public ResponseEntity<?> getNumberOfAlbumsInSystem() {
        return ResponseEntity.ok(compositeService.getNumberOfAlbums());
    }

    /**
     * Get statistics for a specific user.
     *
     * @param userId the user's id
     * @return statistics for the user
     */
    @GetMapping("/admin/statistics/{userId}")
    public ResponseEntity<?> getUserStatistics(@PathVariable String userId) {
        UserStatistics stats = new UserStatistics();

        User user = userService.findById(userId).orElseThrow(() -> new RuntimeException("No such user exists"));
        stats.setUserInfo(user);

        Map<String, Long> albums = compositeService.getAlbumNamesAndSizes(user);
        stats.setAlbumStats(albums);

        Long spaceUsed = albums.values().stream().mapToLong(Long::longValue).sum();
        stats.setSpaceUsed(spaceUsed);

        return ResponseEntity.ok(stats);
    }

    /**
     * Get the amount of space used in the system.
     *
     * @return the amount of space used in the system
     */
    @GetMapping("/admin/statistics/space")
    public ResponseEntity<?> getSpaceUsedInSystem() {
        return ResponseEntity.ok(compositeService.getSpaceUsedInSystem());
    }

    /**
     * Change the system role of a user.
     *
     * @param userId the user's id
     * @param role   the new role
     * @return response entity
     */
    @PutMapping("/admin/user/{userId}/role/{role}")
    public ResponseEntity<?> changeRole(@PathVariable String userId, @PathVariable String role) {
        if (permissionsService.isContextUser(userId)) {
            return ResponseEntity.badRequest().body("You cannot delete your own account");
        }
        userService.changeSystemRole(userId, role);
        return ResponseEntity.ok("Role changed");
    }

    /**
     * Delete a user.
     *
     * @param userId the user's id
     * @return response entity
     */
    @GetMapping("/admin/delete/user/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        if (permissionsService.isContextUser(userId)) {
            return ResponseEntity.badRequest().body("You cannot delete your own account");
        }
        userService.deleteUser(userId);
        compositeService.deleteUserPhotosAndAlbums(userId);
        return ResponseEntity.ok("User deleted");
    }
}
