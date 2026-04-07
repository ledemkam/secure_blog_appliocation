package com.kte.blog_app.controllers;

import com.kte.blog_app.domain.dto.request.LoginRequest;
import com.kte.blog_app.domain.dto.request.RegisterRequest;
import com.kte.blog_app.domain.dto.response.AuthResponse;
import com.kte.blog_app.services.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/auth")
public class AuthController {

    private final AuthenticationService authenticationService;


    @PostMapping(path = "/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody
                                                     RegisterRequest registerRequest) {
        try {
            UserDetails user = authenticationService.register(
                    registerRequest.getName(),
                    registerRequest.getEmail(),
                    registerRequest.getPassword()
            );

            AuthResponse authResponse = AuthResponse.builder()
                    .token(authenticationService.generateToken(user))
                    .expiresIn(86400) // 24 hours in seconds
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
        } catch (IllegalArgumentException e) {
            // User already exists
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }



    @PostMapping(path = "/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody
                                                  LoginRequest loginRequest) {
        try {
            UserDetails user = authenticationService.authenticate(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            );

            AuthResponse authResponse = AuthResponse.builder()
                    .token(authenticationService.generateToken(user))
                    .expiresIn(86400) // 24 hours in seconds
                    .build();

            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


}
