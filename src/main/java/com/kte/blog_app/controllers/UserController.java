package com.kte.blog_app.controllers;

import com.kte.blog_app.controllers.ui_controllers.IUserController;
import com.kte.blog_app.domain.dto.request.UpdateUserRequest;
import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.exceptions.UserNotFoundException;
import com.kte.blog_app.security.UserSecurityService;
import com.kte.blog_app.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/user")
public class UserController implements IUserController {

    private final UserService userService;
    private final UserSecurityService userSecurityService;

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

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        log.debug("Successfully retrieved user with email: {} and name: '{}'", email, user.getName());
        return ResponseEntity.ok(user);
    }


    @Override
    @PutMapping("/{id}")
    @PreAuthorize("@userSecurityService.canUpdateUser(#id)")  // only usewr can update
    public ResponseEntity<User> updateUser(@PathVariable Long id,
                                           @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        log.info("Received request to update user with ID: {}", id);
        User updatedUser = userService.updateUser(id, updateUserRequest);
        log.info("Successfully updated user with ID: {}", id);
        return ResponseEntity.ok(updatedUser);
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("@userSecurityService.canDeleteUser(#id)")  // only user or admin can delete
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Received request to delete user with ID: {}", id);
        userService.deleteUser(id);
        log.info("Successfully deleted user with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
