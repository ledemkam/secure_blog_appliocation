package com.kte.blog_app.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    @NotBlank (message = "name is required")
    @Size(min = 3, max = 50, message = "name must be between {min} and {max} characters")
    private String name;

    @Email(message = "email format is invalid or email is required")
    private String email;
}
