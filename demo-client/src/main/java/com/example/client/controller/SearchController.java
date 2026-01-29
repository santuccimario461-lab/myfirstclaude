package com.example.client.controller;

import com.example.client.dto.ArticleVO;
import com.example.client.dto.SearchRequest;
import com.example.client.service.SearchService;
import com.example.common.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Client-facing search endpoints.
 * Users can search articles by custom keywords.
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * Full-text keyword search
     */
    @PostMapping
    public ApiResponse<List<ArticleVO>> search(@Valid @RequestBody SearchRequest request) {
        List<ArticleVO> results = searchService.search(
                request.getKeyword(), request.getPage(), request.getSize());
        return ApiResponse.success(results);
    }

    /**
     * GET-based keyword search for convenience
     */
    @GetMapping
    public ApiResponse<List<ArticleVO>> searchByParam(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<ArticleVO> results = searchService.search(keyword, page, size);
        return ApiResponse.success(results);
    }

    /**
     * Browse by category
     */
    @GetMapping("/category/{category}")
    public ApiResponse<List<ArticleVO>> searchByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<ArticleVO> results = searchService.searchByCategory(category, page, size);
        return ApiResponse.success(results);
    }

    /**
     * Get article detail (full content)
     */
    @GetMapping("/article/{id}")
    public ApiResponse<ArticleVO> getArticleDetail(@PathVariable String id) {
        ArticleVO article = searchService.getArticleDetail(id);
        return ApiResponse.success(article);
    }
}
