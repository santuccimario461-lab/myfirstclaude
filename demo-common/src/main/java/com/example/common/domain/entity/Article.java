package com.example.common.domain.entity;

import com.example.common.domain.valueobject.ArticleId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Article aggregate root
 */
@Getter
@Builder
@AllArgsConstructor
public class Article {

    private ArticleId id;
    private String title;
    private String content;
    private String author;
    private String sourceUrl;
    private String sourceName;
    private String category;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Article create(String title, String content, String author,
                                 String sourceUrl, String sourceName, String category) {
        LocalDateTime now = LocalDateTime.now();
        return Article.builder()
                .id(ArticleId.generate())
                .title(title)
                .content(content)
                .author(author)
                .sourceUrl(sourceUrl)
                .sourceName(sourceName)
                .category(category)
                .publishedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void updateContent(String title, String content) {
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateCategory(String category) {
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }
}
