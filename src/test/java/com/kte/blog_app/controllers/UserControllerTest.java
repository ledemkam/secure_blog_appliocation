package com.kte.blog_app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.mappers.UserMapper;
import com.kte.blog_app.security.UserSecurityService;
import com.kte.blog_app.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserSecurityService userSecurityService;

    @MockBean
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;



    // Constants
    private static final String API_BASE_PATH = "/api/v1/user";

    // Common test data
    private User mockUser;
    private Long existingUserId;
    private Long nonExistentUserId;
    private LocalDateTime baseDateTime;

    @BeforeEach
    void setUp() {
        baseDateTime = LocalDateTime.now();
        existingUserId = 1L;
        nonExistentUserId = 999L;

        mockUser = User.builder()
                .id(existingUserId)
                .name("Test User")
                .email("test@test.com")
                .password("encodedPassword")
                .createDate(baseDateTime)
                .posts(new ArrayList<>())
                .build();
    }

    @Test
    void should_return_200_when_user_exists() throws Exception {
        // Given
        when(userService.getUserId(existingUserId)).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/{id}", existingUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingUserId))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@test.com"));

        verify(userService, times(1)).getUserId(existingUserId);
    }

    @Test
    void should_return_200_when_User_by_Email_exist() throws Exception {
        // Given
        when(userService.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/email/{email}", mockUser.getEmail())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingUserId))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@test.com"));

        verify(userService, times(1)).findByEmail(mockUser.getEmail());
    }

    @Test
    void updateUser() {
    }

    @Test
    void deleteUser() {
    }
}