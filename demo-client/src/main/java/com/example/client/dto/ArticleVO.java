package com.example.client.dto;

import com.example.common.domain.entity.Article;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Article view object returned to the client
 */
@Data
@Builder
public class ArticleVO {

    private String id;
    private String title;
    private String summary;
    private String author;
    private String sourceUrl;
    private String sourceName;
    private String category;
    private LocalDateTime publishedAt;

    public static ArticleVO fromDomain(Article article) {
        String content = article.getContent();
        String summary = content.length() > 200 ? content.substring(0, 200) + "..." : content;

        return ArticleVO.builder()
                .id(article.getId().getValue())
                .title(article.getTitle())
                .summary(summary)
                .author(article.getAuthor())
                .sourceUrl(article.getSourceUrl())
                .sourceName(article.getSourceName())
                .category(article.getCategory())
                .publishedAt(article.getPublishedAt())
                .build();
    }
}
