package com.kte.blog_app.services.impl;

import com.kte.blog_app.domain.dto.request.CreatePostRequest;
import com.kte.blog_app.domain.dto.response.AuthorResponse;
import com.kte.blog_app.domain.dto.response.PostResponse;
import com.kte.blog_app.domain.entities.Post;
import com.kte.blog_app.domain.entities.PostStatus;
import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.exceptions.PostNotFoundException;
import com.kte.blog_app.mappers.PostMapper;
import com.kte.blog_app.repositories.PostRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("unit test for PostService")
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostServiceImpl postService;

    //instance variables for test data
    private User author;
    private CreatePostRequest createPostRequest;
    private Post mappedPost;
    private Post savedPost;

    private PostResponse expectedPostResponse;
    private AuthorResponse authorResponse;

    private Post secondPost;
    private PostResponse secondPostResponse;
    private List<Post> postsByCategory;
    private List<PostResponse> expectedPostResponses;



    @BeforeEach
    void setUp() {
        //preparation  for test data
        author = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .password("password1230")
                .createDate(LocalDateTime.now())
                .build();

        createPostRequest = CreatePostRequest.builder()
                .title("My_first_post")
                .content("This is the content of my first blog post")
                .category(PostStatus.DRAFT)
                .build();

        mappedPost = Post.builder()
                .title(createPostRequest.getTitle())
                .content(createPostRequest.getContent())
                .category(createPostRequest.getCategory())
                .build();

        savedPost = Post.builder()
                .id(1L)
                .title(createPostRequest.getTitle())
                .content(createPostRequest.getContent())
                .category(createPostRequest.getCategory())
                .author(author)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();


        authorResponse = AuthorResponse.builder()
                .id(author.getId())
                .name(author.getName())

                .build();

        expectedPostResponse = PostResponse.builder()
                .id(savedPost.getId())
                .title(savedPost.getTitle())
                .content(savedPost.getContent())
                .category(savedPost.getCategory())
                .author(authorResponse)
                .createDate(savedPost.getCreateDate())
                .updateDate(savedPost.getUpdateDate())
                .build();


        secondPost = Post.builder()
                    .id(2L)
                    .title("Second Draft Post")
                    .content("This is the content of the second draft post")
                    .category(PostStatus.DRAFT)
                    .author(author)
                    .createDate(LocalDateTime.now())
                    .updateDate(LocalDateTime.now())
                    .build();

        secondPostResponse = PostResponse.builder()
                    .id(secondPost.getId())
                    .title(secondPost.getTitle())
                    .content(secondPost.getContent())
                    .category(secondPost.getCategory())
                    .author(authorResponse)
                    .createDate(secondPost.getCreateDate())
                    .updateDate(secondPost.getUpdateDate())
                    .build();

        postsByCategory = Arrays.asList(savedPost, secondPost);
        expectedPostResponses = Arrays.asList(expectedPostResponse, secondPostResponse);



    }

    @Test
    @DisplayName("created post wenn User connected")
    void should_create_post_when_user_connected() {
        // Given -mocks_configuration
        when(postMapper.toEntity(createPostRequest)).thenReturn(mappedPost);
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        // When - method for testing
        Post result = postService.createPost(author, createPostRequest);

        // Then - Verifications
        assertNotNull(result);
        assertEquals(savedPost.getId(), result.getId());
        assertEquals(savedPost.getTitle(), result.getTitle());
        assertEquals(savedPost.getContent(), result.getContent());
        assertEquals(savedPost.getCategory(), result.getCategory());
        assertEquals(author, result.getAuthor());

        // Vérification for interactions with mocks
        verify(postMapper, times(1)).toEntity(createPostRequest);
        verify(postRepository, times(1)).save(any(Post.class));

        // Vérify that author  assigned to post
        verify(postRepository).save(argThat(post ->
                post.getAuthor().equals(author) &&
                        post.getTitle().equals(createPostRequest.getTitle()) &&
                        post.getContent().equals(createPostRequest.getContent()) &&
                        post.getCategory().equals(createPostRequest.getCategory())
        ));
    }

    @Test
    @DisplayName("should return post by id when post exists")
    void should_return_post_by_id_when_post_exist() {
        // Given
        Long postId = 1L;
        when(postRepository.findById(postId)).thenReturn(Optional.of(savedPost));
        when(postMapper.toResponse(savedPost)).thenReturn(expectedPostResponse);

        // When
        PostResponse result = postService.getPostById(postId);

        // Then
        assertNotNull(result, "The result should not be null");
        assertEquals(expectedPostResponse.getId(), result.getId());
        assertEquals(expectedPostResponse.getTitle(), result.getTitle());
        assertEquals(expectedPostResponse.getContent(), result.getContent());
        assertEquals(expectedPostResponse.getCategory(), result.getCategory());
        assertEquals(expectedPostResponse.getAuthor().getId(), result.getAuthor().getId());
        assertEquals(expectedPostResponse.getAuthor().getName(), result.getAuthor().getName());

        // Vérify all mocks method
        verify(postRepository, times(1)).findById(postId);
        verify(postMapper, times(1)).toResponse(savedPost);
        verifyNoMoreInteractions(postRepository, postMapper);
    }

    @Test
    @DisplayName("should throw PostNotFoundException when post does not exist")
    void should_throw_exception_when_post_not_found() {
        // Given
        Long notExistentPostId = 999L;
        when(postRepository.findById(notExistentPostId)).thenReturn(Optional.empty());

        // When & Then
        PostNotFoundException exception = assertThrows(
                PostNotFoundException.class,
                () -> postService.getPostById(notExistentPostId),
                "A PostNotFoundException should be thrown when the post does not exist."
        );

        assertEquals("Post not found with id: " + notExistentPostId, exception.getMessage());

        // Vérifications des interactions
        verify(postRepository, times(1)).findById(notExistentPostId);
        verify(postMapper, never()).toResponse(any(Post.class));
        verifyNoMoreInteractions(postRepository, postMapper);
    }

    @Test
    @DisplayName("should return all posts by category when posts exist")
    void should_return_all_post_by_category_when_posts_exist() {
        // Given
        PostStatus category = PostStatus.DRAFT;
        when(postRepository.findAllByCategory(category)).thenReturn(postsByCategory);
        when(postMapper.toResponse(savedPost)).thenReturn(expectedPostResponse);
        when(postMapper.toResponse(secondPost)).thenReturn(secondPostResponse);

        // When
        List<PostResponse> result = postService.getAllPostByCategory(category);

        // Then
        assertNotNull(result, "Le résultat ne devrait pas être null");
        assertFalse(result.isEmpty(), "La liste ne devrait pas être vide");
        assertEquals(2, result.size(), "La liste devrait contenir 2 posts");

        // Vérify first post
        PostResponse firstResult = result.get(0);
        assertEquals(expectedPostResponse.getId(), firstResult.getId());
        assertEquals(expectedPostResponse.getTitle(), firstResult.getTitle());
        assertEquals(expectedPostResponse.getContent(), firstResult.getContent());
        assertEquals(expectedPostResponse.getCategory(), firstResult.getCategory());

        // Vérify second post
        PostResponse secondResult = result.get(1);
        assertEquals(secondPostResponse.getId(), secondResult.getId());
        assertEquals(secondPostResponse.getTitle(), secondResult.getTitle());
        assertEquals(secondPostResponse.getContent(), secondResult.getContent());
        assertEquals(secondPostResponse.getCategory(), secondResult.getCategory());

        // Vérify interactions with mocks
        verify(postRepository, times(1)).findAllByCategory(category);
        verify(postMapper, times(1)).toResponse(savedPost);
        verify(postMapper, times(1)).toResponse(secondPost);
        verifyNoMoreInteractions(postRepository, postMapper);
    }

}
