package com.kte.blog_app.mappers;

import com.kte.blog_app.domain.dto.request.UpdateUserRequest;
import com.kte.blog_app.domain.dto.response.UserResponse;
import com.kte.blog_app.domain.entities.User;

import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * entity to response without password
     */
    UserResponse toResponse(User user);

    /**
     * Updates a User entity with the data from UpdateUserRequest
     * Ignores null values to avoid overwriting existing data
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateUserRequest request, @MappingTarget User user);


}
