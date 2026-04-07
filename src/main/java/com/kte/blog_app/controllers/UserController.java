package com.kte.blog_app.controllers;


import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/user")
public class UserController {

    private final UserService userService;

    @GetMapping
    public String getAllUsers() {
        return "All User";
    }
}
