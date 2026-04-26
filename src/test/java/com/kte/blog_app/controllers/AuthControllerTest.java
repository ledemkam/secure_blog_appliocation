package com.kte.blog_app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kte.blog_app.domain.dto.request.LoginRequest;
import com.kte.blog_app.domain.dto.request.RegisterRequest;
import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.exceptions.UserAlreadyExistsException;
import com.kte.blog_app.security.BlogUserDetails;
import com.kte.blog_app.services.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Custom test configuration to exclude default security
@WebMvcTest(controllers = AuthController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
        })
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_ShouldReturnCreated_WhenValidRequest() throws Exception {
        // Given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .name("testUser")
                .email("test@example.com")
                .password("password1230")
                .build();

        // Create mock user
        User mockUser = User.builder()
                .id(1L)
                .name("testUser")
                .email("test@example.com")
                .password("password1230")
                .createDate(LocalDateTime.now())
                .build();

        BlogUserDetails userDetails = new BlogUserDetails(mockUser);
        when(authenticationService.register(any(), any(), any())).thenReturn(userDetails);
        when(authenticationService.generateToken(any())).thenReturn("mock-jwt-token");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.expiresIn").value(86400));
    }

    @Test
    void register_ShouldReturnConflict_WhenUserAlreadyExists() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .name("testUser")
                .email("existing@example.com")
                .password("password1230")
                .build();

        when(authenticationService.register(any(), any(), any()))
                .thenThrow(new UserAlreadyExistsException("User already exists"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        // Given - Email invalide
        RegisterRequest request = RegisterRequest.builder()
                .name("testUser")
                .email("invalid-email")
                .password("short") // Trop court selon @Size(min = 8)
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ShouldReturnOk_WhenValidCredentials() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password1230")
                .build();

        // Create mock user
        User mockUser = User.builder()
                .id(1L)
                .name("testUser")
                .email("test@example.com")
                .password("password1230")
                .createDate(LocalDateTime.now())
                .build();

        BlogUserDetails userDetails = new BlogUserDetails(mockUser);
        when(authenticationService.authenticate(any(), any())).thenReturn(userDetails);
        when(authenticationService.generateToken(any())).thenReturn("mock-jwt-token");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.expiresIn").value(86400));
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenInvalidCredentials() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("wrong-password")
                .build();

        when(authenticationService.authenticate(any(), any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
