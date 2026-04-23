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
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import com.kte.blog_app.domain.dto.request.UpdateUserRequest;
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
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
        User result = userService.getUserId(testUserId);

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
        assertThatThrownBy(() -> userService.getUserId(nonExistentId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("user not found with id: " + nonExistentId);

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



}