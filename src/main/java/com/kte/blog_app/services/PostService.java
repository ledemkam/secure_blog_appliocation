package com.kte.blog_app.services;


import com.kte.blog_app.domain.dto.request.CreatePostRequest;
import com.kte.blog_app.domain.dto.response.PostResponse;
import com.kte.blog_app.domain.entities.Post;
import com.kte.blog_app.domain.entities.PostStatus;
import com.kte.blog_app.domain.entities.User;

import java.util.List;

public interface PostService {

    Post createPost(User user, CreatePostRequest createPostRequest);
    PostResponse getPostById(Long id);
    List<PostResponse> getAllPostByCategory(PostStatus category);
}
