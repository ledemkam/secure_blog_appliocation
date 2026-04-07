package com.kte.blog_app.services.impl;

import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.exceptions.UserNotFoundException;
import com.kte.blog_app.repositories.UserRepository;
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


//unit test for userService

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUserId_shouldReturnUser_WhenUserExists() {
        // Given
        Long userId = 1L;
        User expectedUser = User.builder()
                .id(userId)
                .email("test@aol.de")
                .name("testUser")
                .password("password1230")
                .createDate(LocalDateTime.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        //When
        User actualUser = userService.getUserId(userId);

        //Then
        assertEquals(expectedUser, actualUser);
        verify(userRepository).findById(userId);

    }

    @Test
    void getUserId_shouldThrowUserNotFoundException_WhenUserDoesNotExist() {
        //Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        //When and Then
        assertThatThrownBy(()-> userService.getUserId(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with  id: " + userId);
    }



}