package com.kte.blog_app.security;

import com.kte.blog_app.domain.entities.Post;
import com.kte.blog_app.domain.entities.PostStatus;
import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostSecurityService {

    private final UserService userService;

    /**
     * Récupère l'utilisateur actuellement authentifié
     */
    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }

        String email = authentication.getName();
        return userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    /**
     * Vérifie si l'utilisateur peut modifier un post
     */
    public void validatePostModificationRights(Post post, User user) {
        if (!post.getAuthor().getId().equals(user.getId())) {
            log.warn("User {} denied access to modify post {} (owner: {})",
                    user.getId(), post.getId(), post.getAuthor().getId());
            throw new AccessDeniedException("You can only modify your own posts");
        }
    }

    /**
     * Vérifie si l'utilisateur peut accéder à un post (pour des futures fonctionnalités)
     */
    public boolean canAccessPost(Post post, User user) {
        // Logique d'accès plus complexe si nécessaire
        return post.getCategory() == PostStatus.PUBLISHED ||
                post.getAuthor().getId().equals(user.getId());
    }
}
