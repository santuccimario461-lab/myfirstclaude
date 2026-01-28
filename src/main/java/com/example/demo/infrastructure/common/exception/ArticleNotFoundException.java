package com.example.demo.infrastructure.common.exception;

/**
 * Exception thrown when article is not found
 */
public class ArticleNotFoundException extends RuntimeException {

    public ArticleNotFoundException(String id) {
        super("Article not found with id: " + id);
    }
}
