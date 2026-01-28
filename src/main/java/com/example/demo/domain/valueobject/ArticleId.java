package com.example.demo.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

/**
 * Article ID Value Object
 */
@Getter
@EqualsAndHashCode
public class ArticleId {

    private final String value;

    private ArticleId(String value) {
        this.value = value;
    }

    public static ArticleId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ArticleId cannot be null or empty");
        }
        return new ArticleId(value);
    }

    public static ArticleId generate() {
        return new ArticleId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
