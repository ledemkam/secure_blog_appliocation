package com.kte.blog_app.services.impl;



import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.repositories.UserRepository;
import com.kte.blog_app.security.BlogUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlogUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BlogUserDetailsService blogUserDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("john@example.com")
                .password("encodedPassword")
                .name("John")
                .createDate(LocalDateTime.now())
                .build();
    }

    @Test
    void loadUserByUsername_whenUserExists_shouldReturnBlogUserDetails() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        UserDetails result = blogUserDetailsService.loadUserByUsername("john@example.com");

        assertThat(result).isInstanceOf(BlogUserDetails.class);
        assertThat(result.getUsername()).isEqualTo("john@example.com");
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    void loadUserByUsername_whenUserNotFound_shouldThrowUsernameNotFoundException() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blogUserDetailsService.loadUserByUsername("ghost@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found with email: ghost@example.com");
    }
}