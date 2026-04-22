package com.kte.blog_app.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

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

    /**
     * Checks if the user has a specific role
     */
    public boolean hasRole(String role) {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
    }
}
