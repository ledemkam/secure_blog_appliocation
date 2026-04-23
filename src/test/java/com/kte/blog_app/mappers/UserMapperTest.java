package com.kte.blog_app.mappers;


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

    @Test
    @DisplayName("toResponse: Should return null when user is null")
    void toResponse_should_return_Null_when_User_IsNull() {
        // When
        UserResponse response = userMapper.toResponse(null);

        // Then
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("toResponse: Should handle user with minimal data")
    void toResponse_should_Handle_Minimal_Data() {
        // Given
        User user = User.builder()
                .id(2L)
                .name("Jane")
                .email("jane@test.com")
                .createDate(null) // Test with null date
                .build();

        // When
        UserResponse response = userMapper.toResponse(user);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getName()).isEqualTo("Jane");
        assertThat(response.getEmail()).isEqualTo("jane@test.com");
        assertThat(response.getCreateDate()).isNull();
    }



}