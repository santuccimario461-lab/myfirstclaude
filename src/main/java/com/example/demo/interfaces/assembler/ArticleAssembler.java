package com.example.demo.interfaces.assembler;

import com.example.demo.application.command.CrawlArticleCommand;
import com.example.demo.application.command.CreateArticleCommand;
import com.example.demo.domain.entity.Article;
import com.example.demo.interfaces.dto.request.CrawlArticleRequest;
import com.example.demo.interfaces.dto.request.CreateArticleRequest;
import com.example.demo.interfaces.dto.response.ArticleResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Assembler for converting between DTOs and Domain objects
 */
@Component
public class ArticleAssembler {

    public CreateArticleCommand toCommand(CreateArticleRequest request) {
        return CreateArticleCommand.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(request.getAuthor())
                .sourceUrl(request.getSourceUrl())
                .build();
    }

    public CrawlArticleCommand toCommand(CrawlArticleRequest request) {
        return CrawlArticleCommand.builder()
                .url(request.getUrl())
                .build();
    }

    public ArticleResponse toResponse(Article article) {
        return ArticleResponse.builder()
                .id(article.getId().getValue())
                .title(article.getTitle())
                .content(article.getContent())
                .author(article.getAuthor())
                .sourceUrl(article.getSourceUrl())
                .publishedAt(article.getPublishedAt())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }

    public List<ArticleResponse> toResponseList(List<Article> articles) {
        return articles.stream()
                .map(this::toResponse)
                .toList();
    }
}
