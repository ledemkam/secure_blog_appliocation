package com.kte.blog_app.services.impl;

import com.kte.blog_app.domain.dto.request.UpdateUserRequest;
import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.exceptions.UserNotFoundException;
import com.kte.blog_app.mappers.UserMapper;
import com.kte.blog_app.repositories.UserRepository;
import com.kte.blog_app.security.AuthorizationService;
import com.kte.blog_app.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthorizationService authorizationService;

    String userNotFoundMessage = "user not found with id: ";

    @Override
    public User getUserId(Long id) {
        log.debug("Getting user by id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(userNotFoundMessage + id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> getAllUsers() {
        log.debug("Getting all users");
        return userRepository.findAll();
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public User updateUser(Long id, UpdateUserRequest updateUserRequest) {
        // ✅ Récupérer l'utilisateur directement sans PostSecurityService
        User currentUser = getCurrentAuthenticatedUser();
        log.info("User {} attempting to update user with id: {}", currentUser.getId(), id);

        // check permit via AuthorizationService
        if (!authorizationService.canAccessResource(currentUser.getId(), id)) {
            throw new AccessDeniedException("You can only update your own profile");
        }

        // Retrieve the user to modify
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(userNotFoundMessage + id));

        // apply changes with mapper
        userMapper.updateEntity(updateUserRequest, existingUser);

        // Save and return
        User updatedUser = userRepository.save(existingUser);
        log.info("Successfully updated user with id: {}", id);

        return updatedUser;
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void deleteUser(Long id) {
        // ✅ Récupérer l'utilisateur directement sans PostSecurityService
        User currentUser = getCurrentAuthenticatedUser();
        log.info("User {} attempting to delete user with id: {}", currentUser.getId(), id);

        // check all permit via AuthorizationService
        if (!authorizationService.canAccessResource(currentUser.getId(), id)) {
            throw new AccessDeniedException("You can only delete your own account");
        }

        // check exist user
        User userToDelete = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(userNotFoundMessage + id));

        // deleting
        userRepository.delete(userToDelete);
        log.info("Successfully deleted user with id: {}", id);
    }

    /**
     * ✅ Méthode privée pour récupérer l'utilisateur connecté sans dépendance circulaire
     */
    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }
}