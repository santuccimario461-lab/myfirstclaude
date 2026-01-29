package com.example.common.domain.event;

import com.example.common.domain.valueobject.ArticleId;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Domain event: articles indexed into ES
 */
@Getter
public class ArticleIndexedEvent {

    private final ArticleId articleId;
    private final String title;
    private final String sourceName;
    private final LocalDateTime occurredAt;

    public ArticleIndexedEvent(ArticleId articleId, String title, String sourceName) {
        this.articleId = articleId;
        this.title = title;
        this.sourceName = sourceName;
        this.occurredAt = LocalDateTime.now();
    }
}
