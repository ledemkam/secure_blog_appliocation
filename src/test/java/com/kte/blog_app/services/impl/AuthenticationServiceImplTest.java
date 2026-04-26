package com.kte.blog_app.services.impl;

import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.exceptions.UserAlreadyExistsException;
import com.kte.blog_app.repositories.UserRepository;
import com.kte.blog_app.security.BlogUserDetails;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    // Même secret que dans test/resources/application.yaml
    private static final String SECRET_KEY = "dGVzdF9qd3Rfc2VjcmV0X2Zvcl90ZXN0aW5nX3B1cnBvc2VzX29ubHk=";
    private static final long EXPIRATION_MS = 86400000L; // 24h

    private User user;
    private BlogUserDetails blogUserDetails;

    @BeforeEach
    void setUp() {
        // Injection des @Value via ReflectionTestUtils (pas de Spring context)
        ReflectionTestUtils.setField(authenticationService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(authenticationService, "jwtExpirationMs", EXPIRATION_MS);

        user = User.builder()
                .id(1L)
                .email("john@example.com")
                .password("encodedPassword")
                .name("John")
                .createDate(LocalDateTime.now())
                .build();

        blogUserDetails = new BlogUserDetails(user);
    }

    // ── authenticate ──────────────────────────────────────────────────────────

    @Test
    void authenticate_withValidCredentials_shouldReturnUserDetails() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null); // authenticationManager retourne un Authentication, null suffit ici
        when(userDetailsService.loadUserByUsername("john@example.com"))
                .thenReturn(blogUserDetails);

        UserDetails result = authenticationService.authenticate("john@example.com", "password");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("john@example.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void authenticate_withInvalidCredentials_shouldThrowBadCredentialsException() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authenticationService.authenticate("john@example.com", "wrongpassword"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    // ── generateToken ─────────────────────────────────────────────────────────

    @Test
    void generateToken_shouldReturnNonNullToken() {
        String token = authenticationService.generateToken(blogUserDetails);

        assertThat(token).isNotNull().isNotBlank();
    }


    // ── validateToken ─────────────────────────────────────────────────────────

    @Test
    void validateToken_withValidToken_shouldReturnUserDetails() {
        // Génère un vrai token
        String token = authenticationService.generateToken(blogUserDetails);

        when(userDetailsService.loadUserByUsername("john@example.com"))
                .thenReturn(blogUserDetails);

        UserDetails result = authenticationService.validateToken(token);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("john@example.com");
    }

    @Test
    void validateToken_withInvalidToken_shouldThrowRuntimeException() {
        assertThatThrownBy(() -> authenticationService.validateToken("invalid.token.here"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid JWT token");
    }

    @Test
    void validateToken_withExpiredToken_shouldReturnNull() {
        // Token avec expiration à -1ms (déjà expiré)
        ReflectionTestUtils.setField(authenticationService, "jwtExpirationMs", -1L);
        String expiredToken = authenticationService.generateToken(blogUserDetails);

        // Remet l'expiration normale pour que le parsing fonctionne mais détecte l'expiration
        // Note: JJWT lève ExpiredJwtException → capturé par le catch → RuntimeException
        assertThatThrownBy(() -> authenticationService.validateToken(expiredToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid JWT token");
    }

    // ── register ──────────────────────────────────────────────────────────────

    @Test
    void register_withNewEmail_shouldReturnBlogUserDetails() {
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDetails result = authenticationService.register("John", "new@example.com", "rawPassword");

        assertThat(result).isInstanceOf(BlogUserDetails.class);
        assertThat(result.getUsername()).isEqualTo("john@example.com");

        verify(passwordEncoder).encode("rawPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_withExistingEmail_shouldThrowUserAlreadyExistsException() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authenticationService.register("John", "john@example.com", "password"))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User with email john@example.com already exists");

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }
}