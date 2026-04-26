package com.kte.blog_app.services.impl;

import com.kte.blog_app.domain.dto.request.CreatePostRequest;
import com.kte.blog_app.domain.dto.request.UpdatePostRequest;
import com.kte.blog_app.domain.dto.response.PostResponse;
import com.kte.blog_app.domain.entities.Post;
import com.kte.blog_app.domain.entities.PostStatus;
import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.exceptions.PostNotFoundException;
import com.kte.blog_app.mappers.PostMapper;
import com.kte.blog_app.repositories.PostRepository;
import com.kte.blog_app.security.PostSecurityService;
import com.kte.blog_app.services.PostService;
import com.kte.blog_app.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

   private final PostRepository postRepository;
   private final PostMapper postMapper;
   private final PostSecurityService postSecurityService;

   String postNotFoundMessage = "Post not found with id: ";
   private final UserService userService;

    @Transactional
    @Override
    public Post createPost(User user, CreatePostRequest createPostRequest) {
        log.info("User {} creating new Post: {}", user.getId(), createPostRequest.getTitle());

        Post post = postMapper.toEntity(createPostRequest);
        post.setAuthor(user);
        Post createdPost = postRepository.save(post);

        log.info("Created post with id: {} by user: {}", createdPost.getId(), user.getId());
        return createdPost;
    }

    @Override
    public PostResponse getPostById(Long id) {
        log.debug("Getting post by id: {}", id);

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(postNotFoundMessage + id));
        return postMapper.toResponse(post);
    }

    @Override
    public List<PostResponse> getAllPostByCategory(PostStatus category) {
        log.debug("Getting posts by category: {}", category);

        List<Post> posts = postRepository.findAllByCategory(category);
        return posts.stream()
                .map(postMapper::toResponse)
                .toList();
    }

    @Override
    public List<PostResponse> getAllPostByAuthorAndCategory(User author, PostStatus category) {
        log.debug("Getting posts by author: {} and category: {}", author.getName(), category);
        List<Post> posts = postRepository.findAllByAuthorAndCategory(author, category);
        return posts.stream()
                .map(postMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public PostResponse updatePost(Long id, UpdatePostRequest updatePostRequest) {
        User currentUser = postSecurityService.getCurrentAuthenticatedUser();
        log.info("User {} attempting to update post with id: {}", currentUser.getId(), id);

        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Post not found with id: " + id));

        // Check authorization
        postSecurityService.validatePostModificationRights(existingPost, currentUser);

        postMapper.updateEntity(updatePostRequest, existingPost);
        Post updatedPost = postRepository.save(existingPost);

        log.info("Successfully updated post {} by user {}", updatedPost.getId(), currentUser.getId());
        return postMapper.toResponse(updatedPost);
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void deletePost(Long id) {
        User currentUser = postSecurityService.getCurrentAuthenticatedUser();
        log.info("User {} attempting to delete post with id: {}", currentUser.getId(), id);

        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(postNotFoundMessage + id));

        // Check authorization
        postSecurityService.validatePostModificationRights(existingPost, currentUser);

        postRepository.deleteById(id);
        log.info("Successfully deleted post {} by user {}", id, currentUser.getId());
    }
}