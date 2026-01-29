package com.example.client.service;

import com.example.client.dto.ArticleVO;
import com.example.common.domain.entity.Article;
import com.example.common.domain.repository.ArticleRepository;
import com.example.common.domain.valueobject.ArticleId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Client-facing search service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ArticleRepository articleRepository;

    public List<ArticleVO> search(String keyword, int page, int size) {
        log.info("User search: keyword='{}', page={}, size={}", keyword, page, size);
        List<Article> articles = articleRepository.search(keyword, page, size);
        return articles.stream().map(ArticleVO::fromDomain).toList();
    }

    public List<ArticleVO> searchByCategory(String category, int page, int size) {
        List<Article> articles = articleRepository.findByCategory(category, page, size);
        return articles.stream().map(ArticleVO::fromDomain).toList();
    }

    public ArticleVO getArticleDetail(String id) {
        Article article = articleRepository.findById(ArticleId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Article not found: " + id));
        // Return full content for detail view
        return ArticleVO.builder()
                .id(article.getId().getValue())
                .title(article.getTitle())
                .summary(article.getContent())
                .author(article.getAuthor())
                .sourceUrl(article.getSourceUrl())
                .sourceName(article.getSourceName())
                .category(article.getCategory())
                .publishedAt(article.getPublishedAt())
                .build();
    }
}
