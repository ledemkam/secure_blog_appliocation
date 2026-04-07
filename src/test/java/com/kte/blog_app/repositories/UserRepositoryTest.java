package com.kte.blog_app.repositories;

import com.kte.blog_app.domain.entities.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;


//Test d integration

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByEmail_shouldReturnUser_WhenEmailExists() {
        //Given
        User user = User.builder()
                .email("test@aol.de")
                .name("testUser")
                .password("password1230")
                .createDate(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(user);

        //When
        Optional<User> foundUser = userRepository.findByEmail("test@aol.de");

        //Then
        assertTrue(foundUser.isPresent());
        assertEquals(user, foundUser.get());
        assertThat(foundUser.get().getEmail()).isEqualTo("test@aol.de");

    }
}