package dev.kapiaszczyk.mpp.services;

import dev.kapiaszczyk.mpp.errors.OperationError;
import dev.kapiaszczyk.mpp.models.SystemRoles;
import dev.kapiaszczyk.mpp.models.api.SharedUsersInfo;
import dev.kapiaszczyk.mpp.models.database.User;
import dev.kapiaszczyk.mpp.repositories.UserRepository;
import dev.kapiaszczyk.mpp.util.Either;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static dev.kapiaszczyk.mpp.errors.GenericErrors.*;

@Service
public class UserService implements UserDetailsService {

    UserRepository userRepository;
    HashingService hashingService;

    public UserService(HashingService hashingService, UserRepository userRepository) {
        this.hashingService = hashingService;
        this.userRepository = userRepository;
    }

    public Either<OperationError, User> createUser(String email, String username, String password) {
        // If this is the first user, create user with admin role
        SystemRoles role = userRepository.count() == 0 ? SystemRoles.ADMIN : SystemRoles.USER;
        if (isEmailUnique(email) && isUsernameUnique(username)) {
            String hashedPassword = hashingService.hashPassword(password);
            User user = new User(email, username, hashedPassword, List.of(role.name()));
            return Either.ofRight(userRepository.save(user));
        } else {
            return Either.ofLeft(OperationError.badRequest(USER_ALREADY_EXISTS));
        }
    }

    public void deleteUser(String userId) {
        Optional<User> user = userRepository.findById(userId);
        user.ifPresent(userRepository::delete);
    }

    private boolean isEmailUnique(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }

    private boolean isUsernameUnique(String username) {
        return userRepository.findByUsername(username).isEmpty();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(NO_SUCH_USER));
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getAuthorities()
        );
    }

    public Optional<User> findById(String userId) {
        return userRepository.findById(userId);
    }

    public long getNumberOfUsers() {
        return userRepository.count();
    }

    public List<String> getRoles() {
        return SystemRoles.getAllRoles();
    }

    public Either<OperationError, ?> changeSystemRole(String userId, String role) {
        if (!isRoleValid(role)) {
            return Either.ofLeft(OperationError.badRequest(INVALID_ROLE));
        }

        Optional<User> user = userRepository.findById(userId);
        user.ifPresent(u -> {
            u.setRoles(List.of(role.toUpperCase().trim()));
            userRepository.save(u);
        });

        return user.map(Either::<OperationError, User>ofRight)
                .orElseGet(() -> Either.ofLeft(OperationError.notFound(NO_SUCH_USER)));
    }

    private boolean isRoleValid(String role) {
        return SystemRoles.isRoleValid(role);
    }

    public List<SharedUsersInfo> getSharedUsersInfo() {
        List<User> users = userRepository.findAll();
        List<SharedUsersInfo> userInfo = new ArrayList<>();
        for (User u : users) {
            userInfo.add(new SharedUsersInfo(u.getId(), u.getUsername(), u.getAuthorities().toString()));
        }
        return userInfo;
    }

}
