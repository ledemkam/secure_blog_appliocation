package com.kte.blog_app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kte.blog_app.domain.dto.request.CreatePostRequest;
import com.kte.blog_app.domain.dto.response.AuthorResponse;
import com.kte.blog_app.domain.dto.response.PostResponse;
import com.kte.blog_app.domain.entities.Post;
import com.kte.blog_app.domain.entities.PostStatus;
import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.mappers.PostMapper;
import com.kte.blog_app.security.PostSecurityService;
import com.kte.blog_app.services.PostService;
import com.kte.blog_app.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;


import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @MockBean
    private PostSecurityService postSecurityService;

    @MockBean
    private PostMapper postMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@test.com")
                .createDate(LocalDateTime.now()) // past createDate
                .posts(new ArrayList<>()) // past list of post
                .build();
    }

    // --- POST ---
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void Should_Return_201_When_post_created() throws Exception {
        // Given - Creating test data
        CreatePostRequest createPostRequest = CreatePostRequest.builder()
                .title("Test Post Title")
                .content("This is a test post content with sufficient length for validation")
                .category(PostStatus.DRAFT)
                .build();

        // Mock Post avec author
        Post mockPost = Post.builder()
                .id(1L)
                .title("Test Post Title")
                .content("This is a test post content with sufficient length for validation")
                .category(PostStatus.DRAFT)
                .author(mockUser)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        // Mock PostResponse
        AuthorResponse authorResponse = AuthorResponse.builder()
                .id(1L)
                .name("Test User")
                .build();

        PostResponse expectedResponse = PostResponse.builder()
                .id(1L)
                .title("Test Post Title")
                .content("This is a test post content with sufficient length for validation")
                .category(PostStatus.DRAFT)
                .author(authorResponse)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        // Mock services
        when(postSecurityService.getCurrentAuthenticatedUser()).thenReturn(mockUser);
        when(postService.createPost(any(User.class), any(CreatePostRequest.class))).thenReturn(mockPost);
        when(postMapper.toResponse(any(Post.class))).thenReturn(expectedResponse);

        // When & Then - Test execution
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPostRequest))
                        .with(csrf())) // AJOUTER CSRF TOKEN
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Post Title"))
                .andExpect(jsonPath("$.content").value("This is a test post content with sufficient length for validation"))
                .andExpect(jsonPath("$.category").value("DRAFT"));

        // Vérifications
        verify(postService, times(1)).createPost(any(User.class), any(CreatePostRequest.class));
        verify(postSecurityService, times(1)).getCurrentAuthenticatedUser();
        verify(postMapper, times(1)).toResponse(any(Post.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void Should_Return_400_invalid_request() throws Exception {
        // Given - request with no titel
        CreatePostRequest invalidRequest = CreatePostRequest.builder()
                .content("This is a test post content with sufficient length for validation")
                .category(PostStatus.DRAFT)
                // .missing titel !
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        // Vérifier qu'aucun service n'est appelé
        verify(postService, never()).createPost(any(), any());
    }
}