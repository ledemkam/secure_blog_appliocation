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
        Long nonExistentPostId = 999L;
        when(postRepository.findById(nonExistentPostId)).thenReturn(Optional.empty());

        // When & Then
        PostNotFoundException exception = assertThrows(
                PostNotFoundException.class,
                () -> postService.getPostById(nonExistentPostId),
                "A PostNotFoundException should be thrown when the post does not exist."
        );

        assertEquals("Post not found with id: " + nonExistentPostId, exception.getMessage());

        // Vérifications des interactions
        verify(postRepository, times(1)).findById(nonExistentPostId);
        verify(postMapper, never()).toResponse(any(Post.class));
        verifyNoMoreInteractions(postRepository, postMapper);
    }



}
