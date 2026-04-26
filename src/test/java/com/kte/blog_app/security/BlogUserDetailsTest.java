package com.kte.blog_app.security;

import com.kte.blog_app.domain.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class BlogUserDetailsTest {

    private User user;
    private BlogUserDetails blogUserDetails;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("john@example.com")
                .password("encodedPassword")
                .name("John Doe")
                .createDate(LocalDateTime.now())
                .build();

        blogUserDetails = new BlogUserDetails(user);
    }

    @Test
    void getAuthorities() {
        Collection<? extends GrantedAuthority> authorities = blogUserDetails.getAuthorities();

        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    void getPassword() {
        assertThat(blogUserDetails.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    void getUsername() {
        // getUsername() retourne l'email de l'utilisateur
        assertThat(blogUserDetails.getUsername()).isEqualTo("john@example.com");
    }

    @Test
    void isAccountNonExpired() {
        assertThat(blogUserDetails.isAccountNonExpired()).isTrue();
    }

    @Test
    void isAccountNonLocked() {
        assertThat(blogUserDetails.isAccountNonLocked()).isTrue();
    }

    @Test
    void isCredentialsNonExpired() {
        assertThat(blogUserDetails.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void isEnabled() {
        assertThat(blogUserDetails.isEnabled()).isTrue();
    }

    @Test
    void getId() {
        assertThat(blogUserDetails.getId()).isEqualTo(1L);
    }

    @Test
    void getUser() {
        assertThat(blogUserDetails.getUser()).isEqualTo(user);
    }
}