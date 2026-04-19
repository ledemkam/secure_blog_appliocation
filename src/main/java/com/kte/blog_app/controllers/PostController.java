package com.kte.blog_app.controllers;

import com.kte.blog_app.controllers.ui_controllers.IpostController;
import com.kte.blog_app.domain.dto.request.CreatePostRequest;
import com.kte.blog_app.domain.dto.response.PostResponse;
import com.kte.blog_app.domain.entities.Post;
import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.mappers.PostMapper;
import com.kte.blog_app.security.PostSecurityService;
import com.kte.blog_app.services.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "api/v1/posts")
public class PostController implements IpostController {

    private final PostService postService;
    private final PostSecurityService postSecurityService;
    private final PostMapper postMapper;

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


}
