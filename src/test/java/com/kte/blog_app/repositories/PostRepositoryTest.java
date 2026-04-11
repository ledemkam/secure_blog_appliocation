package com.kte.blog_app.repositories;


import com.kte.blog_app.domain.entities.Post;
import com.kte.blog_app.domain.entities.PostStatus;
import com.kte.blog_app.domain.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@TestPropertySource(
        properties = {
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.datasource.url=jdbc:h2:mem:testdb",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password="

        }
)
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Post old_brochures;
    private Post new_post;

    @BeforeEach
    void setUp() {
        // create valid user
        User author1 = User.builder()
                .email("author1@example.com")
                .password("password1230")
                .name("Author_One")
                .createDate(LocalDateTime.now())
                .build();

        User author2 = User.builder()
                .email("author2@example.com")
                .password("password1230")
                .name("Author_Two")
                .createDate(LocalDateTime.now())
                .build();

        // Before user muss persist
        entityManager.persist(author1);
        entityManager.persist(author2);

        // and create post with persist user
        old_brochures = Post.builder()
                .title("old brochures")
                .category(PostStatus.DRAFT)
                .content("the older paper von brochure")
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .author(author1)
                .build();

        new_post = Post.builder()
                .title("today_news")
                .category(PostStatus.PUBLISHED)
                .content("TODAY'S NEWSPAPER")
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .author(author2)
                .build();

        entityManager.persist(old_brochures);
        entityManager.persist(new_post);
        entityManager.flush();
    }


    @Test
    @DisplayName("should find all posts by status")
    void should_find_posts_by_status() {

        //when
        List<Post> posts = postRepository.findAllByCategory(PostStatus.DRAFT);

        //Then
        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).getCategory()).isEqualTo(PostStatus.DRAFT);
        assertThat(posts.get(0).getTitle()).isEqualTo("old brochures");
    }

    //@Test
   // void should_find_posts_by_author_and_Status() {
    //}

}
