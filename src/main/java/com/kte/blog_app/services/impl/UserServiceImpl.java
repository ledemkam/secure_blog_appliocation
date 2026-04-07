package com.kte.blog_app.services.impl;

import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.exceptions.UserNotFoundException;
import com.kte.blog_app.repositories.UserRepository;
import com.kte.blog_app.services.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getUserId(Long id) {
        return userRepository.findById(id).orElseThrow(() ->new UserNotFoundException
                ("User not found with  id: " + id));
    }
}
