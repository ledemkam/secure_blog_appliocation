package com.kte.blog_app.security;

import com.kte.blog_app.domain.entities.Post;
import com.kte.blog_app.domain.entities.PostStatus;
import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.exceptions.PostNotFoundException;
import com.kte.blog_app.repositories.PostRepository;
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
    private final PostRepository postRepository; // ✅ Accès direct au repository

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
     * Vérifie si l'utilisateur peut accéder à un post
     */
    public boolean canAccessPost(Post post, User user) {
        return post.getCategory() == PostStatus.PUBLISHED ||
                post.getAuthor().getId().equals(user.getId());
    }

    /**
     * Vérifie si l'utilisateur peut modifier un post
     */
    public boolean canUpdatePost(Long postId) {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new PostNotFoundException("Post not found with id: " + postId));
            return post.getAuthor().getId().equals(currentUser.getId());
        } catch (Exception e) {
            log.error("Error checking update permissions for post {}: {}", postId, e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie si l'utilisateur peut supprimer un post
     */
    public boolean canDeletePost(Long postId) {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            Post post = postRepository.findById(postId) // ✅ Récupère sans supprimer
                    .orElseThrow(() -> new PostNotFoundException("Post not found with id: " + postId));
            return post.getAuthor().getId().equals(currentUser.getId());
        } catch (Exception e) {
            log.error("Error checking delete permissions for post {}: {}", postId, e.getMessage());
            return false;
        }
    }
}
