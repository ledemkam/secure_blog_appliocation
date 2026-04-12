package com.kte.blog_app.services.impl;

import com.kte.blog_app.domain.dto.request.CreatePostRequest;
import com.kte.blog_app.domain.entities.Post;
import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.mappers.PostMapper;
import com.kte.blog_app.repositories.PostRepository;
import com.kte.blog_app.services.PostService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

   private final PostRepository postRepository;
   private final PostMapper  postMapper;

    @Transactional
    @Override
    public Post createPost(User user, CreatePostRequest createPostRequest) {
        log.info("user created new Post : {}", createPostRequest.getTitle());
      //covertion and saving
        Post post = postMapper.toEntity(createPostRequest);
        post.setAuthor(user);
        Post createdPost = postRepository.save(post);
        log.info("created post with id: {}", createdPost.getId());
        return createdPost;
    }
}

