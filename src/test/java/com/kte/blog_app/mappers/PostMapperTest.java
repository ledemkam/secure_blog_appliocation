package com.kte.blog_app.mappers;

import com.kte.blog_app.domain.dto.request.CreatePostRequest;
import com.kte.blog_app.domain.dto.request.UpdatePostRequest;
import com.kte.blog_app.domain.dto.response.AuthorResponse;
import com.kte.blog_app.domain.dto.response.PostResponse;
import com.kte.blog_app.domain.entities.Post;
import com.kte.blog_app.domain.entities.PostStatus;
import com.kte.blog_app.domain.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@DisplayName("Test with PostMapper")
class PostMapperTest {

    @Autowired
    private PostMapper postMapper;


    @Test
    @DisplayName("toResponse: All fields are corretly mapped")
    void toResponse_shouldMapAllFields(){
        User author = User.builder()
                .id(1L).name("author")
                .email("muster@email.de")
                .password("password")
                .createDate(LocalDateTime.now())
                .build();

        Post post = Post.builder()
                .id(10L).title("title").content("content")
                .author(author).category(PostStatus.PUBLISHED)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        PostResponse response = postMapper.toResponse(post);

        // Vérifications du Post
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getTitle()).isEqualTo("title");
        assertThat(response.getContent()).isEqualTo("content");
        assertThat(response.getCategory()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(response.getCreateDate()).isEqualTo(post.getCreateDate());
        assertThat(response.getUpdateDate()).isEqualTo(post.getUpdateDate());
        
        // Vérifications de l'auteur
        assertThat(response.getAuthor()).isNotNull();
        assertThat(response.getAuthor().getId()).isEqualTo(1L);
        assertThat(response.getAuthor().getName()).isEqualTo("author");
    }

    @Test
    @DisplayName("toResponse: Returns null when post is null")
    void toResponse_shouldReturnNull_whenPostIsNull() {
        PostResponse response = postMapper.toResponse(null);
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("toResponse: Author is null when post has no author")
    void toResponse_shouldHaveNullAuthor_whenPostAuthorIsNull() {
        Post post = Post.builder()
                .id(2L)
                .title("Post sans auteur")
                .content("Contenu")
                .category(PostStatus.DRAFT)
                .build();

        PostResponse response = postMapper.toResponse(post);

        assertThat(response).isNotNull();
        assertThat(response.getAuthor()).isNull();
    }

    @Test
    @DisplayName("toAuthorResponse: Maps only id and name")
    void toAuthorResponse_shouldMapOnlyIdAndName() {
        User user = User.builder()
                .id(5L)
                .name("Bob")
                .email("bob@example.com")
                .password("motdepasse")
                .createDate(LocalDateTime.now())
                .build();

        AuthorResponse response = postMapper.toAuthorResponse(user);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getName()).isEqualTo("Bob");
        // email et password ne sont pas exposés dans AuthorResponse
    }

    @Test
    @DisplayName("toAuthorResponse: Returns null when user is null")
    void toAuthorResponse_shouldReturnNull_whenUserIsNull() {
        AuthorResponse response = postMapper.toAuthorResponse(null);
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("toEntity: Maps request fields to post")
    void toEntity_shouldMapRequestFieldsToPost() {
        CreatePostRequest request = CreatePostRequest.builder()
                .title("Nouveau post")
                .content("Contenu minimal de dix caractères")
                .category(PostStatus.DRAFT)
                .build();

        Post post = postMapper.toEntity(request);

        assertThat(post).isNotNull();
        assertThat(post.getTitle()).isEqualTo("Nouveau post");
        assertThat(post.getContent()).isEqualTo("Contenu minimal de dix caractères");
        assertThat(post.getCategory()).isEqualTo(PostStatus.DRAFT);
        // id et author ne sont pas dans la requête
        assertThat(post.getId()).isNull();
        assertThat(post.getAuthor()).isNull();
    }

    @Test
    @DisplayName("toEntity: Returns null when request is null")
    void toEntity_shouldReturnNull_whenRequestIsNull() {
        Post post = postMapper.toEntity(null);
        assertThat(post).isNull();
    }

    @Test
    @DisplayName("updateEntity: Updates only non-null fields")
    void updateEntity_shouldUpdateOnlyNonNullFields() {
        Post existingPost = Post.builder()
                .id(7L)
                .title("Titre original")
                .content("Contenu original")
                .category(PostStatus.DRAFT)
                .build();

        UpdatePostRequest request = UpdatePostRequest.builder()
                .id(7L)
                .title("Titre mis à jour")
                .content(null) // null → doit être IGNORÉ
                .category(PostStatus.PUBLISHED)
                .build();

        postMapper.updateEntity(request, existingPost);

        assertThat(existingPost.getTitle()).isEqualTo("Titre mis à jour");
        assertThat(existingPost.getContent()).isEqualTo("Contenu original"); // non écrasé
        assertThat(existingPost.getCategory()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(existingPost.getId()).isEqualTo(7L);
    }

    @Test
    @DisplayName("updateEntity: No changes when all request fields are null")
    void updateEntity_shouldChangeNothing_whenAllRequestFieldsAreNull() {
        Post existingPost = Post.builder()
                .id(8L)
                .title("Titre inchangé")
                .content("Contenu inchangé")
                .category(PostStatus.DRAFT)
                .build();

        UpdatePostRequest emptyRequest = UpdatePostRequest.builder().build();

        postMapper.updateEntity(emptyRequest, existingPost);

        assertThat(existingPost.getTitle()).isEqualTo("Titre inchangé");
        assertThat(existingPost.getContent()).isEqualTo("Contenu inchangé");
        assertThat(existingPost.getCategory()).isEqualTo(PostStatus.DRAFT);
    }
}