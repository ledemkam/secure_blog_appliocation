package com.kte.blog_app.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDto {
    private String error;
    private String path;           // ex: /api/v1/posts/5
    private int status;            // ex: 404
    private LocalDateTime timestamp; // ex: 2026-05-02T14:32:10
}
