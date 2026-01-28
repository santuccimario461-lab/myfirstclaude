package com.example.demo.interfaces.controller;

import com.example.demo.application.query.ArticleQuery;
import com.example.demo.application.service.ArticleApplicationService;
import com.example.demo.domain.entity.Article;
import com.example.demo.interfaces.assembler.ArticleAssembler;
import com.example.demo.interfaces.dto.request.CrawlArticleRequest;
import com.example.demo.interfaces.dto.request.CreateArticleRequest;
import com.example.demo.interfaces.dto.response.ApiResponse;
import com.example.demo.interfaces.dto.response.ArticleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Article REST Controller
 */
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleApplicationService articleService;
    private final ArticleAssembler assembler;

    @PostMapping
    public ApiResponse<ArticleResponse> createArticle(@Valid @RequestBody CreateArticleRequest request) {
        Article article = articleService.createArticle(assembler.toCommand(request));
        return ApiResponse.success("Article created successfully", assembler.toResponse(article));
    }

    @PostMapping("/crawl")
    public ApiResponse<ArticleResponse> crawlArticle(@Valid @RequestBody CrawlArticleRequest request) {
        Article article = articleService.crawlAndCreateArticle(assembler.toCommand(request));
        return ApiResponse.success("Article crawled and created successfully", assembler.toResponse(article));
    }

    @GetMapping("/{id}")
    public ApiResponse<ArticleResponse> getArticle(@PathVariable String id) {
        Article article = articleService.getArticle(id);
        return ApiResponse.success(assembler.toResponse(article));
    }

    @GetMapping
    public ApiResponse<List<ArticleResponse>> listArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Article> articles = articleService.listArticles(page, size);
        return ApiResponse.success(assembler.toResponseList(articles));
    }

    @GetMapping("/search")
    public ApiResponse<List<ArticleResponse>> searchArticles(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        ArticleQuery query = ArticleQuery.builder()
                .keyword(keyword)
                .page(page)
                .size(size)
                .build();
        List<Article> articles = articleService.searchArticles(query);
        return ApiResponse.success(assembler.toResponseList(articles));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteArticle(@PathVariable String id) {
        articleService.deleteArticle(id);
        return ApiResponse.success("Article deleted successfully", null);
    }
}
