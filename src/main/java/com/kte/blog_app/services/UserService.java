package com.kte.blog_app.services;

import com.kte.blog_app.domain.entities.User;

import java.util.Optional;

public interface UserService {
    User getUserId(Long id);
    Optional<User> findByEmail(String email);
}