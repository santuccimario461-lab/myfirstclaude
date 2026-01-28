package com.example.demo.domain.repository;

import com.example.demo.domain.entity.Article;
import com.example.demo.domain.valueobject.ArticleId;

import java.util.List;
import java.util.Optional;

/**
 * Article Repository Interface (Domain Layer)
 */
public interface ArticleRepository {

    Article save(Article article);

    Optional<Article> findById(ArticleId id);

    List<Article> findAll(int page, int size);

    List<Article> search(String keyword, int page, int size);

    void deleteById(ArticleId id);

    boolean existsById(ArticleId id);
}
