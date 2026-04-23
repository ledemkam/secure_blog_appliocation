package com.kte.blog_app.mappers;

import com.kte.blog_app.domain.dto.request.UpdateUserRequest;
import com.kte.blog_app.domain.dto.response.UserResponse;
import com.kte.blog_app.domain.entities.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Tests for UserMapper")
class UserMapperTest {


    @Autowired
    private UserMapper userMapper;


    @Test
    @DisplayName("toResponse: Should map all fields correctly but exclude password")
    void should_map_entity_to_Response() {
        // Given
        LocalDateTime createDate = LocalDateTime.now();
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .password("hashedPassword123")
                .createDate(createDate)
                .build();

        // When
        UserResponse response = userMapper.toResponse(user);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(response.getCreateDate()).isEqualTo(createDate);

        // Password should NOT be included in response (security check)
        // UserResponse doesn't have a password field, so this is guaranteed by design
    }
}