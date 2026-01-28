package com.example.demo.domain.event;

import com.example.demo.domain.valueobject.ArticleId;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Domain Event: Article Created
 */
@Getter
public class ArticleCreatedEvent {

    private final ArticleId articleId;
    private final String title;
    private final LocalDateTime occurredAt;

    public ArticleCreatedEvent(ArticleId articleId, String title) {
        this.articleId = articleId;
        this.title = title;
        this.occurredAt = LocalDateTime.now();
    }
}
