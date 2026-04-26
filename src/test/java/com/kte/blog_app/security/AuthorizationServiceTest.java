package com.kte.blog_app.security;

import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthorizationService authorizationService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void authenticateAs(String email, String... roles) {
        List<SimpleGrantedAuthority> authorities = List.of(roles)
                .stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(email, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private User buildUser(Long id, String email) {
        return User.builder()
                .id(id)
                .email(email)
                .password("encoded")
                .name("John")
                .createDate(LocalDateTime.now())
                .build();
    }

    // ── isCurrentUserAdmin ────────────────────────────────────────────────────

    @Test
    void isCurrentUserAdmin_whenUserHasRoleAdmin_shouldReturnTrue() {
        authenticateAs("admin@example.com", "ROLE_ADMIN");

        assertThat(authorizationService.isCurrentUserAdmin()).isTrue();
    }

    @Test
    void isCurrentUserAdmin_whenUserHasOnlyRoleUser_shouldReturnFalse() {
        authenticateAs("user@example.com", "ROLE_USER");

        assertThat(authorizationService.isCurrentUserAdmin()).isFalse();
    }

    // ── canAccessResource ─────────────────────────────────────────────────────

    @Test
    void canAccessResource_whenCurrentUserIsOwner_shouldReturnTrue() {
        authenticateAs("user@example.com", "ROLE_USER");

        assertThat(authorizationService.canAccessResource(1L, 1L)).isTrue();
    }

    @Test
    void canAccessResource_whenCurrentUserIsAdmin_shouldReturnTrue() {
        authenticateAs("admin@example.com", "ROLE_ADMIN");

        // Même si l'utilisateur n'est pas propriétaire, admin peut accéder
        assertThat(authorizationService.canAccessResource(1L, 99L)).isTrue();
    }

    @Test
    void canAccessResource_whenNotOwnerAndNotAdmin_shouldReturnFalse() {
        authenticateAs("user@example.com", "ROLE_USER");

        assertThat(authorizationService.canAccessResource(1L, 99L)).isFalse();
    }

    // ── getCurrentAuthenticatedUser ───────────────────────────────────────────

    @Test
    void getCurrentAuthenticatedUser_whenAuthenticated_shouldReturnUser() {
        authenticateAs("john@example.com", "ROLE_USER");
        User user = buildUser(1L, "john@example.com");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        User result = authorizationService.getCurrentAuthenticatedUser();

        assertThat(result).isEqualTo(user);
    }

    @Test
    void getCurrentAuthenticatedUser_whenNotAuthenticated_shouldThrowAccessDeniedException() {
        // Pas d'authentification dans le SecurityContext
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> authorizationService.getCurrentAuthenticatedUser())
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Authentication required");
    }

    @Test
    void getCurrentAuthenticatedUser_whenUserNotFoundInDb_shouldThrowRuntimeException() {
        authenticateAs("ghost@example.com", "ROLE_USER");

        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorizationService.getCurrentAuthenticatedUser())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Authenticated user not found");
    }
}