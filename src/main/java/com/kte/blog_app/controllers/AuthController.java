package com.kte.blog_app.controllers;

import com.kte.blog_app.domain.dto.request.LoginRequest;
import com.kte.blog_app.domain.dto.request.RegisterRequest;
import com.kte.blog_app.domain.dto.response.AuthResponse;
import com.kte.blog_app.services.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/auth")
public class AuthController {

    private static final long MILLISECONDS_PER_SECOND = 1000L;

    private final AuthenticationService authenticationService;


    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    @PostMapping(path = "/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        UserDetails user = authenticationService.register(
                registerRequest.getName(),
                registerRequest.getEmail(),
                registerRequest.getPassword()
        );

        AuthResponse authResponse = AuthResponse.builder()
                .token(authenticationService.generateToken(user))
                .expiresIn(jwtExpirationMs / MILLISECONDS_PER_SECOND)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @PostMapping(path = "/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        UserDetails user = authenticationService.authenticate(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );

        AuthResponse authResponse = AuthResponse.builder()
                .token(authenticationService.generateToken(user))
                .expiresIn(jwtExpirationMs / MILLISECONDS_PER_SECOND)
                .build();

        return ResponseEntity.ok(authResponse);
    }
}