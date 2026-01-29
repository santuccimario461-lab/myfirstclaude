package com.example.admin.service;

import com.example.admin.dto.ArticleDetailDTO;
import com.example.admin.dto.DashboardStats;
import com.example.common.domain.entity.Article;
import com.example.common.domain.repository.ArticleRepository;
import com.example.common.domain.valueobject.ArticleId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Admin service for managing all indexed articles
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminArticleService {

    private final ArticleRepository articleRepository;

    public DashboardStats getDashboardStats() {
        long total = articleRepository.count();
        return DashboardStats.builder()
                .totalArticles(total)
                .build();
    }

    public List<ArticleDetailDTO> listArticles(int page, int size) {
        List<Article> articles = articleRepository.findAll(page, size);
        return articles.stream().map(ArticleDetailDTO::fromDomain).toList();
    }

    public List<ArticleDetailDTO> searchArticles(String keyword, int page, int size) {
        List<Article> articles = articleRepository.search(keyword, page, size);
        return articles.stream().map(ArticleDetailDTO::fromDomain).toList();
    }

    public List<ArticleDetailDTO> listBySource(String sourceName, int page, int size) {
        List<Article> articles = articleRepository.findBySourceName(sourceName, page, size);
        return articles.stream().map(ArticleDetailDTO::fromDomain).toList();
    }

    public List<ArticleDetailDTO> listByCategory(String category, int page, int size) {
        List<Article> articles = articleRepository.findByCategory(category, page, size);
        return articles.stream().map(ArticleDetailDTO::fromDomain).toList();
    }

    public ArticleDetailDTO getArticle(String id) {
        Article article = articleRepository.findById(ArticleId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Article not found: " + id));
        return ArticleDetailDTO.fromDomain(article);
    }

    public void deleteArticle(String id) {
        ArticleId articleId = ArticleId.of(id);
        if (!articleRepository.existsById(articleId)) {
            throw new IllegalArgumentException("Article not found: " + id);
        }
        articleRepository.deleteById(articleId);
        log.info("Admin deleted article: {}", id);
    }

    public void updateArticleCategory(String id, String category) {
        Article article = articleRepository.findById(ArticleId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Article not found: " + id));
        article.updateCategory(category);
        articleRepository.save(article);
        log.info("Admin updated article {} category to {}", id, category);
    }
}
