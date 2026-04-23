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

    @Test
    @DisplayName("updateEntity: Should update only non-null fields")
    void shoul_map_update_Entity() {
        // Given
        User existingUser = User.builder()
                .id(3L)
                .name("OriginalName")
                .email("original@email.com")
                .password("originalPassword")
                .createDate(LocalDateTime.now().minusDays(1))
                .build();

        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("UpdatedName")
                .email("updated@email.com")
                .build();

        // When
        userMapper.updateEntity(request, existingUser);

        // Then
        assertThat(existingUser.getName()).isEqualTo("UpdatedName");
        assertThat(existingUser.getEmail()).isEqualTo("updated@email.com");

        // These fields should remain unchanged
        assertThat(existingUser.getId()).isEqualTo(3L);
        assertThat(existingUser.getPassword()).isEqualTo("originalPassword");
        assertThat(existingUser.getCreateDate()).isNotNull();
    }

    @Test
    @DisplayName("updateEntity: Should ignore null values in update request")
    void update_Entity_should_Ignore_Null_Values() {
        // Given
        User existingUser = User.builder()
                .id(4L)
                .name("KeepThisName")
                .email("keep@this.email")
                .password("keepPassword")
                .createDate(LocalDateTime.now().minusDays(2))
                .build();

        UpdateUserRequest requestWithNulls = UpdateUserRequest.builder()
                .name(null) // Should be ignored
                .email("updated@email.com") // Should be applied
                .build();

        // When
        userMapper.updateEntity(requestWithNulls, existingUser);

        // Then
        assertThat(existingUser.getName()).isEqualTo("KeepThisName"); // Unchanged
        assertThat(existingUser.getEmail()).isEqualTo("updated@email.com"); // Updated
        assertThat(existingUser.getPassword()).isEqualTo("keepPassword"); // Unchanged
        assertThat(existingUser.getId()).isEqualTo(4L); // Unchanged
    }

    @Test
    @DisplayName("updateEntity: Should handle completely null request")
    void update_Entity_should_Handle_Completely_Null_Request() {
        // Given
        LocalDateTime originalCreateDate = LocalDateTime.now().minusDays(3);
        User existingUser = User.builder()
                .id(5L)
                .name("UnchangedName")
                .email("unchanged@email.com")
                .password("unchangedPassword")
                .createDate(originalCreateDate)
                .build();

        UpdateUserRequest emptyRequest = UpdateUserRequest.builder().build();

        // When
        userMapper.updateEntity(emptyRequest, existingUser);

        // Then - Nothing should change
        assertThat(existingUser.getId()).isEqualTo(5L);
        assertThat(existingUser.getName()).isEqualTo("UnchangedName");
        assertThat(existingUser.getEmail()).isEqualTo("unchanged@email.com");
        assertThat(existingUser.getPassword()).isEqualTo("unchangedPassword");
        assertThat(existingUser.getCreateDate()).isEqualTo(originalCreateDate);
    }

    @Test
    @DisplayName("updateEntity: Should update only name when email is null")
    void update_Entity_should_Update_Only_Name_when_Email_Is_Null() {
        // Given
        User existingUser = User.builder()
                .id(6L)
                .name("OldName")
                .email("keep@email.com")
                .password("password123")
                .build();

        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("NewName")
                .email(null) // Should be ignored
                .build();

        // When
        userMapper.updateEntity(request, existingUser);

        // Then
        assertThat(existingUser.getName()).isEqualTo("NewName");
        assertThat(existingUser.getEmail()).isEqualTo("keep@email.com"); // Unchanged
    }

}