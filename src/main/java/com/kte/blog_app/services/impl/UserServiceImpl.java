package com.kte.blog_app.services.impl;

import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.exceptions.UserNotFoundException;
import com.kte.blog_app.repositories.UserRepository;
import com.kte.blog_app.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getUserId(Long id) {
        log.debug("Getting user by id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }
}
