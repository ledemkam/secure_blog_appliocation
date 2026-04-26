package com.kte.blog_app.controllers;

import com.kte.blog_app.controllers.ui_controllers.IpostController;
import com.kte.blog_app.domain.dto.request.CreatePostRequest;
import com.kte.blog_app.domain.dto.request.UpdatePostRequest;
import com.kte.blog_app.domain.dto.response.PostResponse;
import com.kte.blog_app.domain.entities.Post;
import com.kte.blog_app.domain.entities.PostStatus;
import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.mappers.PostMapper;
import com.kte.blog_app.security.PostSecurityService;
import com.kte.blog_app.services.PostService;
import com.kte.blog_app.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "api/v1/posts")
public class PostController implements IpostController {

    private final PostService postService;
    private final PostSecurityService postSecurityService;
    private final PostMapper postMapper;
    private final UserService userService;

    @Override
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody CreatePostRequest createPostRequest) {
        log.info("Received request to create post with title: '{}'", createPostRequest.getTitle());

        User currentUser = postSecurityService.getCurrentAuthenticatedUser();
        log.debug("Authenticated user: '{}' (ID: {}) is creating a post",
                currentUser.getName(), currentUser.getId());

        Post createdPost = postService.createPost(currentUser, createPostRequest);
        PostResponse createdPostResponse = postMapper.toResponse(createdPost);

        log.info("Successfully created post with ID: {} and title: '{}' for user: '{}' (ID: {})",
                createdPost.getId(),
                createdPost.getTitle(),
                currentUser.getName(),
                currentUser.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdPostResponse);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        log.info("Received request to get post with ID: {}", id);

        PostResponse postResponse = postService.getPostById(id);

        log.debug("Successfully retrieved post with ID: {} and title: '{}'", id, postResponse.getTitle());

        return ResponseEntity.ok(postResponse);
    }

    @Override
    @GetMapping("/category")
    public ResponseEntity<List<PostResponse>> getAllPostByCategory(@RequestParam PostStatus category) {
        log.info("Received request to get all posts by category: {}", category);

        List<PostResponse> posts = postService.getAllPostByCategory(category);

        log.debug("Successfully retrieved {} posts for category: {}", posts.size(), category);

        return ResponseEntity.ok(posts);
    }


    @Override
    @GetMapping("/search")
    public ResponseEntity<List<PostResponse>> getAllPostByAuthorAndCategory(
            @RequestParam Long authorId,
            @RequestParam PostStatus category) {

        log.info("Received request to get all posts by author ID: {} and category: {}", authorId, category);

        // Retrieve user by ID
        User author = userService.getUserId(authorId);
        log.debug("Found author: '{}' (ID: {})", author.getName(), author.getId());

        // Call the service
        List<PostResponse> posts = postService.getAllPostByAuthorAndCategory(author, category);

        log.debug("Successfully retrieved {} posts for author '{}' (ID: {}) with category: {}",
                posts.size(), author.getName(), author.getId(), category);

        return ResponseEntity.ok(posts);
    }

    @Override
    @PutMapping("/{id}")
    @PreAuthorize("@postSecurityService.canUpdatePost(#id)")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePostRequest updatePostRequest) {
        log.info("Received request to update post with ID: {}", id);

        User currentUser = postSecurityService.getCurrentAuthenticatedUser();
        log.debug("Authenticated user: '{}' (ID: {}) is updating post with ID: {}",
                currentUser.getName(), currentUser.getId(), id);

        PostResponse updatedPostResponse = postService.updatePost(id, updatePostRequest);

        log.info("Successfully updated post with ID: {} and title: '{}' by user: '{}' (ID: {})",
                id,
                updatedPostResponse.getTitle(),
                currentUser.getName(),
                currentUser.getId());

        return ResponseEntity.ok(updatedPostResponse);
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("@postSecurityService.canDeletePost(#id)")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        log.info("Received request to delete post with ID: {}", id);

        User currentUser = postSecurityService.getCurrentAuthenticatedUser();
        log.debug("Authenticated user: '{}' (ID: {}) is attempting to delete post with ID: {}",
                currentUser.getName(), currentUser.getId(), id);

        postService.deletePost(id);

        log.info("Successfully deleted post with ID: {} by user: '{}' (ID: {})",
                id, currentUser.getName(), currentUser.getId());

        return ResponseEntity.noContent().build();
    }


}

