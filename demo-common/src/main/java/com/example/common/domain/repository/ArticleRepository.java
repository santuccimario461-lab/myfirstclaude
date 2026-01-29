package com.example.common.domain.repository;

import com.example.common.domain.entity.Article;
import com.example.common.domain.valueobject.ArticleId;

import java.util.List;
import java.util.Optional;

/**
 * Article repository interface (domain layer)
 */
public interface ArticleRepository {

    Article save(Article article);

    List<Article> saveAll(List<Article> articles);

    Optional<Article> findById(ArticleId id);

    List<Article> findAll(int page, int size);

    List<Article> search(String keyword, int page, int size);

    List<Article> findBySourceName(String sourceName, int page, int size);

    List<Article> findByCategory(String category, int page, int size);

    void deleteById(ArticleId id);

    boolean existsById(ArticleId id);

    long count();
}
