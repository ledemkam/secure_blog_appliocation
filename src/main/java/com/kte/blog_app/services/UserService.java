package com.kte.blog_app.services;

import com.kte.blog_app.domain.dto.request.UpdateUserRequest;
import com.kte.blog_app.domain.entities.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User getUserId(Long id);
    Optional<User> findByEmail(String email);
    List<User> getAllUsers();
    User updateUser(Long id, UpdateUserRequest updateUserRequest);
    void deleteUser(Long id);

}