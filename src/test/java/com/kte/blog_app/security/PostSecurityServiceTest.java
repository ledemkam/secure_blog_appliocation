package com.kte.blog_app.security;




import com.kte.blog_app.domain.entities.Post;
import com.kte.blog_app.domain.entities.PostStatus;
import com.kte.blog_app.domain.entities.User;

import com.kte.blog_app.repositories.PostRepository;
import com.kte.blog_app.services.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostSecurityServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostSecurityService postSecurityService;

    private User owner;
    private User otherUser;
    private Post post;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).email("owner@example.com")
                .password("pwd").name("Owner").createDate(LocalDateTime.now()).build();

        otherUser = User.builder().id(2L).email("other@example.com")
                .password("pwd").name("Other").createDate(LocalDateTime.now()).build();

        post = Post.builder().id(10L).title("Title").content("Content")
                .author(owner).category(PostStatus.PUBLISHED)
                .createDate(LocalDateTime.now()).updateDate(LocalDateTime.now()).build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String email) {
        var auth = new UsernamePasswordAuthenticationToken(
                email, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ── getCurrentAuthenticatedUser ───────────────────────────────────────────

    @Test
    void getCurrentAuthenticatedUser_whenAuthenticated_shouldReturnUser() {
        authenticateAs("owner@example.com");
        when(userService.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));

        User result = postSecurityService.getCurrentAuthenticatedUser();

        assertThat(result).isEqualTo(owner);
    }

    @Test
    void getCurrentAuthenticatedUser_whenNotAuthenticated_shouldThrowAccessDeniedException() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> postSecurityService.getCurrentAuthenticatedUser())
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Authentication required");
    }

    @Test
    void getCurrentAuthenticatedUser_whenUserNotInDb_shouldThrowRuntimeException() {
        authenticateAs("ghost@example.com");
        when(userService.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postSecurityService.getCurrentAuthenticatedUser())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Authenticated user not found");
    }

    // ── validatePostModificationRights ────────────────────────────────────────

    @Test
    void validatePostModificationRights_whenOwner_shouldNotThrow() {
        assertThatCode(() -> postSecurityService.validatePostModificationRights(post, owner))
                .doesNotThrowAnyException();
    }

    @Test
    void validatePostModificationRights_whenNotOwner_shouldThrowAccessDeniedException() {
        assertThatThrownBy(() -> postSecurityService.validatePostModificationRights(post, otherUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You can only modify your own posts");
    }

    // ── canAccessPost ─────────────────────────────────────────────────────────

    @Test
    void canAccessPost_whenPostIsPublished_shouldReturnTrue() {
        post.setCategory(PostStatus.PUBLISHED);

        assertThat(postSecurityService.canAccessPost(post, otherUser)).isTrue();
    }

    @Test
    void canAccessPost_whenPostIsDraftAndUserIsOwner_shouldReturnTrue() {
        post.setCategory(PostStatus.DRAFT);

        assertThat(postSecurityService.canAccessPost(post, owner)).isTrue();
    }

    @Test
    void canAccessPost_whenPostIsDraftAndUserIsNotOwner_shouldReturnFalse() {
        post.setCategory(PostStatus.DRAFT);

        assertThat(postSecurityService.canAccessPost(post, otherUser)).isFalse();
    }

    // ── canUpdatePost ─────────────────────────────────────────────────────────

    @Test
    void canUpdatePost_whenCurrentUserIsOwner_shouldReturnTrue() {
        authenticateAs("owner@example.com");
        when(userService.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        assertThat(postSecurityService.canUpdatePost(10L)).isTrue();
    }

    @Test
    void canUpdatePost_whenCurrentUserIsNotOwner_shouldReturnFalse() {
        authenticateAs("other@example.com");
        when(userService.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        assertThat(postSecurityService.canUpdatePost(10L)).isFalse();
    }

    @Test
    void canUpdatePost_whenPostNotFound_shouldReturnFalse() {
        authenticateAs("owner@example.com");
        when(userService.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(postSecurityService.canUpdatePost(99L)).isFalse();
    }

    @Test
    void canUpdatePost_whenExceptionThrown_shouldReturnFalse() {
        authenticateAs("owner@example.com");
        when(userService.findByEmail("owner@example.com")).thenThrow(new RuntimeException("DB error"));

        assertThat(postSecurityService.canUpdatePost(10L)).isFalse();
    }

    // ── canDeletePost ─────────────────────────────────────────────────────────

    @Test
    void canDeletePost_whenCurrentUserIsOwner_shouldReturnTrue() {
        authenticateAs("owner@example.com");
        when(userService.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        assertThat(postSecurityService.canDeletePost(10L)).isTrue();
    }

    @Test
    void canDeletePost_whenCurrentUserIsNotOwner_shouldReturnFalse() {
        authenticateAs("other@example.com");
        when(userService.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        assertThat(postSecurityService.canDeletePost(10L)).isFalse();
    }

    @Test
    void canDeletePost_whenPostNotFound_shouldReturnFalse() {
        authenticateAs("owner@example.com");
        when(userService.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(postSecurityService.canDeletePost(99L)).isFalse();
    }

    @Test
    void canDeletePost_whenExceptionThrown_shouldReturnFalse() {
        authenticateAs("owner@example.com");
        when(userService.findByEmail("owner@example.com")).thenThrow(new RuntimeException("DB error"));

        assertThat(postSecurityService.canDeletePost(10L)).isFalse();
    }
}