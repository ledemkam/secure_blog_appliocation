package com.kte.blog_app.config;

import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    public ApplicationRunner initializeData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String email = "user@test.com";
            userRepository.findByEmail(email).ifPresentOrElse(
                    user -> {}, // Utilisateur existe déjà
                    () -> {
                        User newUser = User.builder()
                                .name("Test User")
                                .email(email)
                                .password(passwordEncoder.encode("defaultPassword123"))
                                .createDate(LocalDateTime.now())
                                .build();
                        userRepository.save(newUser);
                        System.out.println("✅ Utilisateur test créé: " + email);
                    }
            );
        };
    }
}

