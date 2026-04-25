package com.kte.blog_app.security;

import com.kte.blog_app.domain.entities.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.kte.blog_app.services.UserService;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSecurityService {

    private final UserService userService;
    private final AuthorizationService authorizationService;

    /**
     * Vérifie si l'utilisateur courant peut supprimer le compte (lui-même OU admin)
     */
    public boolean canDeleteUser(Long targetUserId) {
        try {
            User currentUser = authorizationService.getCurrentAuthenticatedUser();
            return authorizationService.canAccessResource(currentUser.getId(), targetUserId);
        } catch (Exception e) {
            log.error("Error checking delete permissions for user {}: {}", targetUserId, e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie si l'utilisateur courant peut modifier le compte (uniquement lui-même)
     */
    public boolean canUpdateUser(Long targetUserId) {
        try {
            User currentUser = authorizationService.getCurrentAuthenticatedUser();
            return currentUser.getId().equals(targetUserId); // Pas d'admin ici
        } catch (Exception e) {
            log.error("Error checking update permissions for user {}: {}", targetUserId, e.getMessage());
            return false;
        }
    }
}
