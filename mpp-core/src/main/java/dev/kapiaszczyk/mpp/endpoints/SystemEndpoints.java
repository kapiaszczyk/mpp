package dev.kapiaszczyk.mpp.endpoints;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * Endpoint for admin operations.
 */
@Tag(
        name = "System Endpoints",
        description = "Operations related to system monitoring, management and statistics. " +
                "These operations can only be performed by users with the ADMIN role in the system."
)
public interface SystemEndpoints {

    /**
     * Get amount of photos in the system.
     *
     * @return response containing amount of photos
     */
    @Operation(
            summary = "Get amount of photos in the system",
            description = "Get amount of photos in the system."
    )
    ResponseEntity<?> getAmountOfPhotos();

    /**
     * Get amount of albums in the system.
     *
     * @return response containing amount of albums
     */
    @Operation(
            summary = "Get amount of albums in the system",
            description = "Get amount of albums in the system."
    )
    ResponseEntity<?> getNumberOfAlbumsInSystem();

    /**
     * Get amount of users in the system.
     *
     * @return response containing amount of users
     */
    @Operation(
            summary = "Get amount of users in the system",
            description = "Get amount of users in the system."
    )
    ResponseEntity<?> getAmountOfUsers();

    /**
     * Get all users in the system.
     *
     * @return response containing list of users
     */
    @Operation(
            summary = "Get all users in the system",
            description = "Get all users in the system."
    )
    ResponseEntity<?> getUsers();

    /**
     * Get all roles in the system.
     *
     * @return response containing list of roles
     */
    @Operation(
            summary = "Get all roles in the system",
            description = "Get all roles in the system."
    )
    ResponseEntity<?> getRoles();

    /**
     * Change role of a user.
     *
     * @return response containing updated user
     */
    @Operation(
            summary = "Change role of a user",
            description = "Change role of a user. This can only be done by a user who has the ADMIN role in the system.",
            method = "PUT",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Role changed successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found"
                    ),
            }
    )
    ResponseEntity<?> changeRole(String userId, String role);

    /**
     * Get statistics of a user.
     *
     * @return response containing user statistics
     */
    ResponseEntity<?> getUserStatistics(String userId);

    /**
     * Get space used in the system.
     *
     * @return response containing used space
     */
    ResponseEntity<?> getSpaceUsedInSystem();

    /**
     * Remove a user from the system and delete all their photos.
     */
    ResponseEntity<?> deleteUser(String userId);

}

