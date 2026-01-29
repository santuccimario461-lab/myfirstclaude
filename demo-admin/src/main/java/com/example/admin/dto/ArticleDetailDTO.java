package com.example.admin.dto;

import com.example.common.domain.entity.Article;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Full article detail for admin view
 */
@Data
@Builder
public class ArticleDetailDTO {

    private String id;
    private String title;
    private String content;
    private String author;
    private String sourceUrl;
    private String sourceName;
    private String category;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ArticleDetailDTO fromDomain(Article article) {
        return ArticleDetailDTO.builder()
                .id(article.getId().getValue())
                .title(article.getTitle())
                .content(article.getContent())
                .author(article.getAuthor())
                .sourceUrl(article.getSourceUrl())
                .sourceName(article.getSourceName())
                .category(article.getCategory())
                .publishedAt(article.getPublishedAt())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }
}
