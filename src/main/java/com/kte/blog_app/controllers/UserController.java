package com.kte.blog_app.controllers;

import com.kte.blog_app.controllers.ui_controllers.IUserController;
import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/user")
public class UserController implements IUserController {

    private final UserService userService;

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        log.info("Received request to get user with ID: {}", id);

        User user = userService.getUserId(id);

        log.debug("Successfully retrieved user with ID: {} and name: '{}'", id, user.getName());

        return ResponseEntity.ok(user);
    }

    @Override
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        log.info("Received request to get user with email: {}", email);

        Optional<User> userOptional = userService.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            log.debug("Successfully retrieved user with email: {} and name: '{}'", email, user.getName());
            return ResponseEntity.ok(user);
        } else {
            log.debug("No user found with email: {}", email);
            return ResponseEntity.notFound().build();
        }
    }
}
