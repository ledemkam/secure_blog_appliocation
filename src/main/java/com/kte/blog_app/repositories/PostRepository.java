package com.kte.blog_app.repositories;

import com.kte.blog_app.domain.entities.Post;
import com.kte.blog_app.domain.entities.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByStatus(PostStatus status);

    List<Post> findAllByAuthorAndStatus(String author, PostStatus status);
}
