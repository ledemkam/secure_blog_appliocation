package com.kte.blog_app.controllers;

import com.kte.blog_app.controllers.ui_controllers.IpostController;
import com.kte.blog_app.domain.dto.request.CreatePostRequest;
import com.kte.blog_app.domain.entities.Post;
import com.kte.blog_app.domain.entities.User;
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

    @Override
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Post> createPost(@Valid @RequestBody CreatePostRequest createPostRequest) {
        User currentUser = postSecurityService.getCurrentAuthenticatedUser();
        Post createdPost = postService.createPost(currentUser, createPostRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

}

