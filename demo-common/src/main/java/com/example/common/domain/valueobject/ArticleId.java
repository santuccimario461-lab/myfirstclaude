package com.example.common.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

/**
 * Article ID value object
 */
@Getter
@EqualsAndHashCode
public class ArticleId implements Serializable {

    private final String value;

    private ArticleId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ArticleId cannot be null or blank");
        }
        this.value = value;
    }

    public static ArticleId generate() {
        return new ArticleId(UUID.randomUUID().toString());
    }

    public static ArticleId of(String value) {
        return new ArticleId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
