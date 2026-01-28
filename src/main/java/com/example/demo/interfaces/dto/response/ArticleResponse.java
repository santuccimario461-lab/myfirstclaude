package com.example.demo.interfaces.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for article
 */
@Data
@Builder
public class ArticleResponse {

    private String id;
    private String title;
    private String content;
    private String author;
    private String sourceUrl;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
