package com.example.admin.controller;

import com.example.admin.dto.ArticleDetailDTO;
import com.example.admin.dto.DashboardStats;
import com.example.admin.service.AdminArticleService;
import com.example.common.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for managing all indexed articles
 */
@RestController
@RequestMapping("/api/admin/articles")
@RequiredArgsConstructor
public class AdminArticleController {

    private final AdminArticleService adminArticleService;

    /**
     * Dashboard overview stats
     */
    @GetMapping("/dashboard")
    public ApiResponse<DashboardStats> dashboard() {
        return ApiResponse.success(adminArticleService.getDashboardStats());
    }

    /**
     * List all articles (paginated)
     */
    @GetMapping
    public ApiResponse<List<ArticleDetailDTO>> listArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(adminArticleService.listArticles(page, size));
    }

    /**
     * Search articles
     */
    @GetMapping("/search")
    public ApiResponse<List<ArticleDetailDTO>> searchArticles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(adminArticleService.searchArticles(keyword, page, size));
    }

    /**
     * Filter by source name
     */
    @GetMapping("/source/{sourceName}")
    public ApiResponse<List<ArticleDetailDTO>> listBySource(
            @PathVariable String sourceName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(adminArticleService.listBySource(sourceName, page, size));
    }

    /**
     * Filter by category
     */
    @GetMapping("/category/{category}")
    public ApiResponse<List<ArticleDetailDTO>> listByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(adminArticleService.listByCategory(category, page, size));
    }

    /**
     * Get article detail
     */
    @GetMapping("/{id}")
    public ApiResponse<ArticleDetailDTO> getArticle(@PathVariable String id) {
        return ApiResponse.success(adminArticleService.getArticle(id));
    }

    /**
     * Delete an article
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteArticle(@PathVariable String id) {
        adminArticleService.deleteArticle(id);
        return ApiResponse.success("Article deleted", null);
    }

    /**
     * Update article category
     */
    @PutMapping("/{id}/category")
    public ApiResponse<Void> updateCategory(
            @PathVariable String id,
            @RequestParam String category) {
        adminArticleService.updateArticleCategory(id, category);
        return ApiResponse.success("Category updated", null);
    }
}
