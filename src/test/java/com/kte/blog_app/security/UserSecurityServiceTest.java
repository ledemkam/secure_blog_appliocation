package com.kte.blog_app.security;

import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSecurityServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private UserSecurityService userSecurityService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().id(1L).email("user@example.com")
                .password("pwd").name("User").createDate(LocalDateTime.now()).build();
    }

    // ── canDeleteUser ─────────────────────────────────────────────────────────

    @Test
    void canDeleteUser_whenCurrentUserIsTarget_shouldReturnTrue() {
        when(authorizationService.getCurrentAuthenticatedUser()).thenReturn(currentUser);
        when(authorizationService.canAccessResource(1L, 1L)).thenReturn(true);

        assertThat(userSecurityService.canDeleteUser(1L)).isTrue();
    }

    @Test
    void canDeleteUser_whenCurrentUserIsAdmin_shouldReturnTrue() {
        when(authorizationService.getCurrentAuthenticatedUser()).thenReturn(currentUser);
        when(authorizationService.canAccessResource(1L, 99L)).thenReturn(true); // admin

        assertThat(userSecurityService.canDeleteUser(99L)).isTrue();
    }

    @Test
    void canDeleteUser_whenNotOwnerAndNotAdmin_shouldReturnFalse() {
        when(authorizationService.getCurrentAuthenticatedUser()).thenReturn(currentUser);
        when(authorizationService.canAccessResource(1L, 99L)).thenReturn(false);

        assertThat(userSecurityService.canDeleteUser(99L)).isFalse();
    }

    @Test
    void canDeleteUser_whenExceptionThrown_shouldReturnFalse() {
        when(authorizationService.getCurrentAuthenticatedUser())
                .thenThrow(new RuntimeException("Auth error"));

        assertThat(userSecurityService.canDeleteUser(1L)).isFalse();
    }

    // ── canUpdateUser ─────────────────────────────────────────────────────────

    @Test
    void canUpdateUser_whenCurrentUserIsTarget_shouldReturnTrue() {
        when(authorizationService.getCurrentAuthenticatedUser()).thenReturn(currentUser);

        assertThat(userSecurityService.canUpdateUser(1L)).isTrue();
    }

    @Test
    void canUpdateUser_whenCurrentUserIsNotTarget_shouldReturnFalse() {
        when(authorizationService.getCurrentAuthenticatedUser()).thenReturn(currentUser);

        assertThat(userSecurityService.canUpdateUser(99L)).isFalse();
    }

    @Test
    void canUpdateUser_whenExceptionThrown_shouldReturnFalse() {
        when(authorizationService.getCurrentAuthenticatedUser())
                .thenThrow(new RuntimeException("Auth error"));

        assertThat(userSecurityService.canUpdateUser(1L)).isFalse();
    }
}