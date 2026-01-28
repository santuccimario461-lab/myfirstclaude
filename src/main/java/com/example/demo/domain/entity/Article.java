package com.example.demo.domain.entity;

import com.example.demo.domain.valueobject.ArticleId;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Article Aggregate Root
 */
@Getter
@Builder
public class Article {

    private ArticleId id;
    private String title;
    private String content;
    private String author;
    private String sourceUrl;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void updateContent(String title, String content) {
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public static Article create(String title, String content, String author, String sourceUrl) {
        LocalDateTime now = LocalDateTime.now();
        return Article.builder()
                .id(ArticleId.generate())
                .title(title)
                .content(content)
                .author(author)
                .sourceUrl(sourceUrl)
                .publishedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
