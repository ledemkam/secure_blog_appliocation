package com.kte.blog_app.services.impl;

import com.kte.blog_app.domain.dto.request.CreatePostRequest;
import com.kte.blog_app.domain.entities.Post;
import com.kte.blog_app.domain.entities.PostStatus;
import com.kte.blog_app.domain.entities.User;
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


}
