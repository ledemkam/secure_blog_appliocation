package com.kte.blog_app.domain.dto.response;
import com.kte.blog_app.domain.entities.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private AuthorResponse author;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private PostStatus category;
}