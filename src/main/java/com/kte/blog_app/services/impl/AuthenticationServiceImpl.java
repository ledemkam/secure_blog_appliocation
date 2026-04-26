package com.kte.blog_app.services.impl;

import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.exceptions.UserAlreadyExistsException;
import com.kte.blog_app.repositories.UserRepository;
import com.kte.blog_app.security.BlogUserDetails;
import com.kte.blog_app.services.AuthenticationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}")  // ← USES the property from application.yaml
    private Long jwtExpirationMs;

    @Override
    public UserDetails authenticate(String email, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            return userDetailsService.loadUserByUsername(email);
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))  // ← Fixed
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public UserDetails validateToken(String token) {
        try {
            String username = extractUsername(token);
            if (username != null && !isTokenExpired(token)) {
                return userDetailsService.loadUserByUsername(username);
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
        return null;
    }

    @Override
    public UserDetails register(String name, String email, String password) {  // ← Fixed signature
        // Check if user already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("User with email " + email + " already exists");
        }

        // Create new user
        User newUser = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .createDate(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(newUser);
        return new BlogUserDetails(savedUser);
    }

    private String extractUsername(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    private boolean isTokenExpired(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration().before(new Date());
    }

    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
