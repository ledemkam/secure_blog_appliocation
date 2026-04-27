package com.kte.blog_app.services.impl;


import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.exceptions.UserNotFoundException;
import com.kte.blog_app.mappers.UserMapper;
import com.kte.blog_app.repositories.UserRepository;
import com.kte.blog_app.security.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;


import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import com.kte.blog_app.domain.dto.request.UpdateUserRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.*;


@ExtendWith(SpringExtension.class)
@DisplayName("UserService Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Long testUserId;
    private String testEmail;

    @BeforeEach
    void setUp() {
        testUserId = 1L;
        testEmail = "test@example.com";
        testUser = User.builder()
                .id(testUserId)
                .name("Test User")
                .email(testEmail)
                .password("hashedPassword123")
                .createDate(LocalDateTime.now().minusDays(1))
                .build();

    }


    @Test
    @DisplayName("Should return user when ID exists")
    void should_return_user_when_id_exist() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserById(testUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUserId);
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getEmail()).isEqualTo(testEmail);
        assertThat(result.getPassword()).isEqualTo("hashedPassword123");
        assertThat(result.getCreateDate()).isNotNull();

        // Verify repository interaction
        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when ID does not exist")
    void should_throw_exception_when_user_id_not_exist() {
        // Given
        Long nonExistentId = 999L;
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(nonExistentId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with id: " + nonExistentId);

        verify(userRepository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("Should return user when email exists")
    void should_return_user_when_email_exists() {
        // Given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByEmail(testEmail);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(testEmail);
        assertThat(result.get().getName()).isEqualTo("Test User");

        verify(userRepository, times(1)).findByEmail(testEmail);
    }

    @Test
    @DisplayName("Should return empty Optional when email does not exist")
    void should_return_empty_when_email_not_exist() {
        // Given
        String nonExistentEmail = "notfound@example.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByEmail(nonExistentEmail);

        // Then
        assertThat(result).isEmpty();

        verify(userRepository, times(1)).findByEmail(nonExistentEmail);
    }

    @Test
    @DisplayName("Should return all users")
    void should_return_all_users() {
        // Given
        User user2 = User.builder()
                .id(2L)
                .name("User Two")
                .email("user2@example.com")
                .password("password456")
                .createDate(LocalDateTime.now())
                .build();

        List<User> expectedUsers = List.of(testUser, user2);
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // When
        List<User> result = userService.getAllUsers();

        // Then

        assertThat(result).containsExactlyInAnyOrder(testUser, user2);

        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should update user successfully when authorized")
    @WithMockUser(username = "test@example.com", roles = "USER")
    void should_update_user_when_authorized() {
        // Given
        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .name("Updated Name")
                .email("updated@example.com")
                .build();

        when(authorizationService.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(authorizationService.canAccessResource(testUserId, testUserId)).thenReturn(true);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        User result = userService.updateUser(testUserId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUserId);

        verify(authorizationService).getCurrentAuthenticatedUser();
        verify(authorizationService).canAccessResource(testUserId, testUserId);
        verify(userRepository).findById(testUserId);
        verify(userMapper).updateEntity(updateRequest, testUser);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should delete user successfully when authorized")
    @WithMockUser(username = "test@example.com", roles = "USER")
    void should_delete_user_when_authorized() {
        // Given
        when(authorizationService.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(authorizationService.canAccessResource(testUserId, testUserId)).thenReturn(true);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When
        userService.deleteUser(testUserId);

        // Then
        verify(authorizationService).getCurrentAuthenticatedUser();
        verify(authorizationService).canAccessResource(testUserId, testUserId);
        verify(userRepository).findById(testUserId);
        verify(userRepository).delete(testUser);
    }

}