package com.kte.blog_app.mappers;
import com.kte.blog_app.domain.entities.Post;
import com.kte.blog_app.domain.entities.User;
import com.kte.blog_app.domain.dto.request.CreatePostRequest;
import com.kte.blog_app.domain.dto.request.UpdatePostRequest;
import com.kte.blog_app.domain.dto.response.AuthorResponse;
import com.kte.blog_app.domain.dto.response.PostResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
@Mapper(componentModel = "spring")
public interface PostMapper {
    PostResponse toResponse(Post post);
    AuthorResponse toAuthorResponse(User user);
    Post toEntity(CreatePostRequest request);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdatePostRequest request, @MappingTarget Post post);
}