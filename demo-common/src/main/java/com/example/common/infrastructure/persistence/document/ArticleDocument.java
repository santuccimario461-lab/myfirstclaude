package com.example.common.infrastructure.persistence.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Elasticsearch document for Article
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDocument {

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
}
