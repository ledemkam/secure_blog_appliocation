package com.kte.blog_app.services;


import com.kte.blog_app.domain.dto.request.CreatePostRequest;
import com.kte.blog_app.domain.entities.Post;
import com.kte.blog_app.domain.entities.User;

public interface PostService {

    Post createPost(User user, CreatePostRequest createPostRequest);
}
