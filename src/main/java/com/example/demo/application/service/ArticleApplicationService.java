package com.example.demo.application.service;

import com.example.demo.application.command.CrawlArticleCommand;
import com.example.demo.application.command.CreateArticleCommand;
import com.example.demo.application.query.ArticleQuery;
import com.example.demo.domain.entity.Article;
import com.example.demo.domain.repository.ArticleRepository;
import com.example.demo.domain.service.ContentParserService;
import com.example.demo.domain.valueobject.ArticleId;
import com.example.demo.infrastructure.common.exception.ArticleNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Article Application Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleApplicationService {

    private final ArticleRepository articleRepository;
    private final ContentParserService contentParserService;

    public Article createArticle(CreateArticleCommand command) {
        log.info("Creating article with title: {}", command.getTitle());

        Article article = Article.create(
                command.getTitle(),
                command.getContent(),
                command.getAuthor(),
                command.getSourceUrl()
        );

        return articleRepository.save(article);
    }

    public Article crawlAndCreateArticle(CrawlArticleCommand command) {
        log.info("Crawling article from URL: {}", command.getUrl());

        ContentParserService.ParsedContent parsedContent = contentParserService.parseFromUrl(command.getUrl());

        Article article = Article.create(
                parsedContent.title(),
                parsedContent.content(),
                parsedContent.author(),
                command.getUrl()
        );

        return articleRepository.save(article);
    }

    public Article getArticle(String id) {
        return articleRepository.findById(ArticleId.of(id))
                .orElseThrow(() -> new ArticleNotFoundException(id));
    }

    public List<Article> listArticles(int page, int size) {
        return articleRepository.findAll(page, size);
    }

    public List<Article> searchArticles(ArticleQuery query) {
        if (query.getKeyword() == null || query.getKeyword().isBlank()) {
            return articleRepository.findAll(query.getPage(), query.getSize());
        }
        return articleRepository.search(query.getKeyword(), query.getPage(), query.getSize());
    }

    public void deleteArticle(String id) {
        ArticleId articleId = ArticleId.of(id);
        if (!articleRepository.existsById(articleId)) {
            throw new ArticleNotFoundException(id);
        }
        articleRepository.deleteById(articleId);
        log.info("Deleted article with id: {}", id);
    }
}
