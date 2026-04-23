package com.kte.blog_app.security;

import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final UserRepository userRepository;

    /**
     * check ,if currentuser is Admin role
     */
    public boolean isCurrentUserAdmin() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Checks if the user can access/modify a resource
     * @param currentUserId ID from coonectd user
     * @param resourceOwnerId ID of the resource owner
     */
    public boolean canAccessResource(Long currentUserId, Long resourceOwnerId) {
        return isCurrentUserAdmin() || currentUserId.equals(resourceOwnerId);
    }

    /**  Retrive connected user (avoid circular dependency)     */
    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

}
