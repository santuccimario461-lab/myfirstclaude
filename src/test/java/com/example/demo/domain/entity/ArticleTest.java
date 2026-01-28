package com.example.demo.domain.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArticleTest {

    @Test
    void shouldCreateArticle() {
        Article article = Article.create(
                "Test Title",
                "Test Content",
                "Test Author",
                "https://example.com"
        );

        assertNotNull(article.getId());
        assertEquals("Test Title", article.getTitle());
        assertEquals("Test Content", article.getContent());
        assertEquals("Test Author", article.getAuthor());
        assertEquals("https://example.com", article.getSourceUrl());
        assertNotNull(article.getCreatedAt());
        assertNotNull(article.getUpdatedAt());
    }

    @Test
    void shouldUpdateContent() {
        Article article = Article.create(
                "Original Title",
                "Original Content",
                "Author",
                "https://example.com"
        );

        article.updateContent("Updated Title", "Updated Content");

        assertEquals("Updated Title", article.getTitle());
        assertEquals("Updated Content", article.getContent());
    }
}
