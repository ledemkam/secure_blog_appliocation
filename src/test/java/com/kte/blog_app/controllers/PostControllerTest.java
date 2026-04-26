package com.kte.blog_app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kte.blog_app.domain.dto.request.CreatePostRequest;
import com.kte.blog_app.domain.dto.request.UpdatePostRequest;
import com.kte.blog_app.domain.dto.response.AuthorResponse;
import com.kte.blog_app.domain.dto.response.PostResponse;
import com.kte.blog_app.domain.entities.Post;
import com.kte.blog_app.domain.entities.PostStatus;
import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.exceptions.PostNotFoundException;
import com.kte.blog_app.mappers.PostMapper;
import com.kte.blog_app.security.PostSecurityService;
import com.kte.blog_app.services.PostService;
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
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(PostController.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestSecurityConfig.class)
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

    // Constants
    private static final String API_BASE_PATH = "/api/v1/posts";
    private static final String VALID_TITLE = "Test Post Title";
    private static final String VALID_CONTENT = "This is a test post content with sufficient length for validation";
    private static final String EXISTING_POST_TITLE = "Existing Post Title";
    private static final String EXISTING_POST_CONTENT = "This is an existing post content";

    // Common test data
    private User mockUser;
    private User mockAuthor;
    private AuthorResponse defaultAuthorResponse;
    private AuthorResponse johnDoeAuthorResponse;
    private Long defaultAuthorId;
    private Long nonExistentPostId;
    private LocalDateTime baseDateTime;

    @BeforeEach
    void setUp() {
        baseDateTime = LocalDateTime.now();
        nonExistentPostId = 999L;
        defaultAuthorId = 1L;

        // Setup users
        mockUser = createUser(1L, "Test User", "test@test.com", baseDateTime);
        mockAuthor = createUser(2L, "John Doe", "john.doe@test.com", baseDateTime.minusMonths(1));

        // Setup author responses
        defaultAuthorResponse = createAuthorResponse(1L, "Test User");
        johnDoeAuthorResponse = createAuthorResponse(2L, "John Doe");
    }

    // --- Utility Methods ---
    private User createUser(Long id, String name, String email, LocalDateTime createDate) {
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .createDate(createDate)
                .posts(new ArrayList<>())
                .build();
    }

    private AuthorResponse createAuthorResponse(Long id, String name) {
        return AuthorResponse.builder()
                .id(id)
                .name(name)
                .build();
    }

    private CreatePostRequest createValidPostRequest() {
        return CreatePostRequest.builder()
                .title(VALID_TITLE)
                .content(VALID_CONTENT)
                .category(PostStatus.DRAFT)
                .build();
    }

    private CreatePostRequest createInvalidPostRequest() {
        return CreatePostRequest.builder()
                .content(VALID_CONTENT)
                .category(PostStatus.DRAFT)
                // missing title
                .build();
    }

    private Post createMockPost(Long id, String title, String content, PostStatus category, User author) {
        return Post.builder()
                .id(id)
                .title(title)
                .content(content)
                .category(category)
                .author(author)
                .createDate(baseDateTime)
                .updateDate(baseDateTime)
                .build();
    }

    private PostResponse createPostResponse(Long id, String title, String content,
                                            PostStatus category, AuthorResponse author,
                                            LocalDateTime createDate, LocalDateTime updateDate) {
        return PostResponse.builder()
                .id(id)
                .title(title)
                .content(content)
                .category(category)
                .author(author)
                .createDate(createDate)
                .updateDate(updateDate)
                .build();
    }

    private List<PostResponse> createExpectedPostsForCategory(PostStatus category) {
        return List.of(
                createPostResponse(1L, "First Published Post", "Content of first published post",
                        category, createAuthorResponse(1L, "Test Author 1"),
                        baseDateTime.minusDays(2), baseDateTime.minusDays(1)),
                createPostResponse(2L, "Second Published Post", "Content of second published post",
                        category, createAuthorResponse(2L, "Test Author 2"),
                        baseDateTime.minusDays(1), baseDateTime)
        );
    }

    private List<PostResponse> createExpectedPostsForAuthorAndCategory(AuthorResponse author, PostStatus category) {
        return List.of(
                createPostResponse(1L, "John's First Published Post", "Content of John's first published post",
                        category, author, baseDateTime.minusDays(5), baseDateTime.minusDays(3)),
                createPostResponse(2L, "John's Second Published Post", "Content of John's second published post",
                        category, author, baseDateTime.minusDays(2), baseDateTime.minusDays(1))
        );
    }

    // --- POST Tests ---
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void Should_Return_201_When_post_created() throws Exception {
        // Given
        CreatePostRequest request = createValidPostRequest();
        Post mockPost = createMockPost(1L, VALID_TITLE, VALID_CONTENT, PostStatus.DRAFT, mockUser);
        PostResponse expectedResponse = createPostResponse(1L, VALID_TITLE, VALID_CONTENT,
                PostStatus.DRAFT, defaultAuthorResponse, baseDateTime, baseDateTime);

        // Mock services
        when(postSecurityService.getCurrentAuthenticatedUser()).thenReturn(mockUser);
        when(postService.createPost(any(User.class), any(CreatePostRequest.class))).thenReturn(mockPost);
        when(postMapper.toResponse(any(Post.class))).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post(API_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value(VALID_TITLE))
                .andExpect(jsonPath("$.content").value(VALID_CONTENT))
                .andExpect(jsonPath("$.category").value("DRAFT"));

        verify(postService, times(1)).createPost(any(User.class), any(CreatePostRequest.class));
        verify(postSecurityService, times(1)).getCurrentAuthenticatedUser();
        verify(postMapper, times(1)).toResponse(any(Post.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void Should_Return_400_invalid_request() throws Exception {
        // Given
        CreatePostRequest invalidRequest = createInvalidPostRequest();

        // When & Then
        mockMvc.perform(post(API_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(postService, never()).createPost(any(), any());
    }

    @Test
    void Should_Return_401_When_User_Not_Authenticated() throws Exception {
        // Given
        CreatePostRequest validRequest = createValidPostRequest();

        // When & Then
        mockMvc.perform(post(API_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(postService, never()).createPost(any(), any());
    }

    // --- GET by ID Tests ---
    @Test
    void Should_Return_200_Post_When_Post_exists() throws Exception {
        // Given
        Long postId = 1L;
        PostResponse expectedResponse = createPostResponse(postId, EXISTING_POST_TITLE, EXISTING_POST_CONTENT,
                PostStatus.PUBLISHED, defaultAuthorResponse, baseDateTime, baseDateTime);

        when(postService.getPostById(postId)).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.title").value(EXISTING_POST_TITLE))
                .andExpect(jsonPath("$.content").value(EXISTING_POST_CONTENT))
                .andExpect(jsonPath("$.category").value("PUBLISHED"))
                .andExpect(jsonPath("$.author.id").value(1L))
                .andExpect(jsonPath("$.author.name").value("Test User"));

        verify(postService, times(1)).getPostById(postId);
    }

    @Test
    void Should_Return_404_Post_When_Post_no_found() throws Exception {
        // Given
        when(postService.getPostById(nonExistentPostId))
                .thenThrow(new PostNotFoundException("Post with id " + nonExistentPostId + " not found"));

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/{id}", nonExistentPostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ""))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Post not found"));

        verify(postService, times(1)).getPostById(nonExistentPostId);
    }

    // --- GET by Category Tests ---
    @Test
    void Should_Return_200_Post_When_Posts_by_Category_exists() throws Exception {
        // Given
        PostStatus category = PostStatus.PUBLISHED;
        List<PostResponse> expectedPosts = createExpectedPostsForCategory(category);

        when(postService.getAllPostByCategory(category)).thenReturn(expectedPosts);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/category")
                        .param("category", category.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("First Published Post"))
                .andExpect(jsonPath("$[0].category").value("PUBLISHED"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].title").value("Second Published Post"))
                .andExpect(jsonPath("$[1].category").value("PUBLISHED"));

        verify(postService, times(1)).getAllPostByCategory(category);
    }

    @Test
    void Should_Return_500_When_Posts_Invalid_Category_Parameter() throws Exception {
        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/category")
                        .param("category", "INVALID_CATEGORY")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ""))
                .andExpect(status().isInternalServerError());

        verify(postService, never()).getAllPostByCategory(any());
    }

    // --- GET by Author and Category Tests ---
    @Test
    void Should_Return_200_Post_When_Posts_by_Author_and_Category_exists() throws Exception {
        // Given
        PostStatus category = PostStatus.PUBLISHED;
        List<PostResponse> expectedPosts = createExpectedPostsForAuthorAndCategory(johnDoeAuthorResponse, category);

        when(userService.getUserId(defaultAuthorId)).thenReturn(mockAuthor);
        when(postService.getAllPostByAuthorAndCategory(mockAuthor, category)).thenReturn(expectedPosts);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/search")
                        .param("authorId", String.valueOf(defaultAuthorId))
                        .param("category", category.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("John's First Published Post"))
                .andExpect(jsonPath("$[0].category").value("PUBLISHED"))
                .andExpect(jsonPath("$[0].author.name").value("John Doe"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].title").value("John's Second Published Post"))
                .andExpect(jsonPath("$[1].category").value("PUBLISHED"))
                .andExpect(jsonPath("$[1].author.name").value("John Doe"));

        verify(userService, times(1)).getUserId(defaultAuthorId);
        verify(postService, times(1)).getAllPostByAuthorAndCategory(mockAuthor, category);
    }

    @Test
    void Should_Return_500_When_Posts_Invalid_author_and_category_Parameter() throws Exception {
        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/search")
                        .param("author", "INVALID_AUTHOR")
                        .param("category", "INVALID_CATEGORY")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ""))
                .andExpect(status().isInternalServerError());

        verify(postService, never()).getAllPostByCategory(any());
        verify(userService, never()).getUserId(any());
    }

    // --- PUT ---
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void should_return_200_when_post_updated() throws Exception {
        // Given
        Long postId = 1L;
        UpdatePostRequest updateRequest = UpdatePostRequest.builder()
                .id(postId)
                .title("Updated Post Title")
                .content("This is an updated post content with sufficient length for validation")
                .category(PostStatus.PUBLISHED)
                .build();

        PostResponse expectedResponse = createPostResponse(
                postId,
                "Updated Post Title",
                "This is an updated post content with sufficient length for validation",
                PostStatus.PUBLISHED,
                defaultAuthorResponse,
                baseDateTime,
                baseDateTime.plusMinutes(5) // different updateDate
        );

        // Mock services
        when(postSecurityService.getCurrentAuthenticatedUser()).thenReturn(mockUser);
        when(postService.updatePost(postId, updateRequest)).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(put(API_BASE_PATH + "/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.title").value("Updated Post Title"))
                .andExpect(jsonPath("$.content").value("This is an updated post content with sufficient length for validation"))
                .andExpect(jsonPath("$.category").value("PUBLISHED"))
                .andExpect(jsonPath("$.author.id").value(1L))
                .andExpect(jsonPath("$.author.name").value("Test User"));

        // Verify interactions
        verify(postSecurityService, times(1)).getCurrentAuthenticatedUser();
        verify(postService, times(1)).updatePost(postId, updateRequest);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void should_return_204_when_post_deleted() throws Exception {
        // Given
        Long postId = 1L;

        // Mock services - uses data from setup
        when(postSecurityService.getCurrentAuthenticatedUser()).thenReturn(mockUser);
        doNothing().when(postService).deletePost(postId);

        // When & Then
        mockMvc.perform(delete(API_BASE_PATH + "/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(content().string("")); // No content in the response

        // Verify interactions
        verify(postSecurityService, times(1)).getCurrentAuthenticatedUser();
        verify(postService, times(1)).deletePost(postId);
    }
}