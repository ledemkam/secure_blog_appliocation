package com.kte.blog_app.security;

import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.services.AuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private BlogUserDetails blogUserDetails;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        User user = User.builder()
                .id(42L)
                .email("john@example.com")
                .password("encoded")
                .name("John")
                .createDate(LocalDateTime.now())
                .build();

        blogUserDetails = new BlogUserDetails(user);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_withValidToken_shouldAuthenticateAndSetUserId() throws Exception {
        // Token valide dans le header Authorization
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(authenticationService.validateToken("valid.jwt.token")).thenReturn(blogUserDetails);

        filter.doFilterInternal(request, response, filterChain);

        // SecurityContext doit contenir l'authentification
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(blogUserDetails);

        // userId doit être positionné en attribut de la requête
        verify(request).setAttribute("userId", 42L);

        // Le filtre doit toujours appeler la chaîne
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withNoAuthorizationHeader_shouldNotAuthenticate() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        // Aucune authentification ne doit être posée
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        // validateToken ne doit jamais être appelé
        verifyNoInteractions(authenticationService);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withInvalidBearerPrefix_shouldNotAuthenticate() throws Exception {
        // Header présent mais sans "Bearer "
        when(request.getHeader("Authorization")).thenReturn("Basic sometoken");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(authenticationService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withInvalidToken_shouldNotAuthenticateAndStillContinueChain() throws Exception {
        // Token invalide → validateToken lève une exception
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token");
        when(authenticationService.validateToken("invalid.token"))
                .thenThrow(new RuntimeException("Invalid JWT"));

        // Ne doit pas propager l'exception
        filter.doFilterInternal(request, response, filterChain);

        // SecurityContext reste vide
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        // La chaîne doit continuer malgré l'erreur
        verify(filterChain).doFilter(request, response);
    }
}