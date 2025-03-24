package dev.kapiaszczyk.mpp.services;

import dev.kapiaszczyk.mpp.models.AlbumAccessRoles;
import dev.kapiaszczyk.mpp.models.database.Album;
import dev.kapiaszczyk.mpp.models.database.User;
import dev.kapiaszczyk.mpp.repositories.AlbumRepository;
import dev.kapiaszczyk.mpp.repositories.UserRepository;
import dev.kapiaszczyk.mpp.util.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static dev.kapiaszczyk.mpp.errors.GenericErrors.NO_SUCH_USER;
import static dev.kapiaszczyk.mpp.errors.GenericErrors.USER_ALREADY_HAS_ACCESS;

/**
 * Service checking permissions
 */
@Service
public class PermissionsService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final AlbumRepository albumRepository;

    public PermissionsService(UserRepository userRepository, AlbumRepository albumRepository) {
        this.userRepository = userRepository;
        this.albumRepository = albumRepository;
    }

    public boolean userHasPermissionToMovePhotos(String currentAlbumId, String targetAlbumId) {
        return isCtxUserOwnerOfAlbum(currentAlbumId) && isCtxUserOwnerOfAlbum(targetAlbumId);
    }

    public boolean isCtxUserOwnerOfAlbum(String albumId) {
        String userId = getUserFromCtx().getId();
        return albumRepository.isUserOwnerOfAlbum(userId, albumId);
    }

    public boolean isUserFromCtxOwnerOfAlbum(String userId) {
        return isContextUser(userId);
    }

    public boolean isUserFromCtxOwnerOfAlbum(Album album) {
        return getUserFromCtx().getId().equals(album.getOwnerId());
    }

    public boolean isUserFromCtxOwnerOrAdmin(Album album) {
        String userId = getUserFromCtx().getId();
        if (Objects.equals(album.getOwnerId(), userId)) {
            return true;
        }
        String role = album.getPermissions().get(userId);
        return Objects.equals(role, AlbumAccessRoles.ADMINISTRATOR.name());
    }

    public boolean isOwnerAdminOrEditorOrViewer(Album album) {
        String userId = getUserFromCtx().getId();
        if (Objects.equals(album.getOwnerId(), userId)) {
            return true;
        }
        String role = album.getPermissions().get(userId);
        return Objects.equals(role, AlbumAccessRoles.ADMINISTRATOR.name()) ||
                Objects.equals(role, AlbumAccessRoles.EDITOR.name()) ||
                Objects.equals(role, AlbumAccessRoles.VIEWER.name());
    }

    public boolean isOwnerAdminOrEditor(Album album) {
        String userId = getUserFromCtx().getId();
        if (Objects.equals(album.getOwnerId(), userId)) {
            return true;
        }
        String role = album.getPermissions().get(userId);
        return Objects.equals(role, AlbumAccessRoles.ADMINISTRATOR.name()) ||
                Objects.equals(role, AlbumAccessRoles.EDITOR.name());
    }

    public User getUserFromCtx() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new RuntimeException("User in context does not exist"));
    }

    public boolean isContextUser(String userId) {
        return getUserFromCtx().getId().equals(userId);
    }

    public boolean isUserOwnerOrAdmin(Album album, String userId) {
        return Objects.equals(album.getOwnerId(), userId) || Objects.equals(album.getPermissions().get(userId), AlbumAccessRoles.ADMINISTRATOR.name());
    }

    /**
     * Checks if user can get access to album
     * - user exists
     * - user doesn't have access to the album already
     * - user is not owner of the album
     *
     * @param userId user id
     * @param album  album
     * @return either error message or user id
     */
    public Either<String, String> canUserGetAccessToAlbum(String userId, Album album) {
        Optional<User> targetUser = userRepository.findById(userId);

        if (targetUser.isEmpty()) {
            return Either.ofLeft(NO_SUCH_USER);
        } else {
            String targetUserId = targetUser.get().getId();
            boolean doesNotHaveAccess = !album.getPermissions().containsKey(targetUserId);
            boolean isNotOwner = !album.getOwnerId().equals(targetUserId);
            return doesNotHaveAccess && isNotOwner ? Either.ofRight(targetUserId) : Either.ofLeft(USER_ALREADY_HAS_ACCESS);
        }
    }

    public User getUserById(String userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException(NO_SUCH_USER));
    }

    public List<User> searchUsers(String query) {
        return userRepository.findByUsernameRegex(query);
    }

}
