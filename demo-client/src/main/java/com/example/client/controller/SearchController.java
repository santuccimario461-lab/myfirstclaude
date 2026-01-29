package com.example.client.controller;

import com.example.client.dto.PostItemVO;
import com.example.client.service.SearchService;
import com.example.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户端搜索接口
 * 用户可以进行关键词搜索、热度排行、高级搜索
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * 多字段关键词搜索
     * GET /api/search?q=关键词
     */
    @GetMapping
    public ApiResponse<List<PostItemVO>> search(@RequestParam("q") String query) {
        return ApiResponse.success(searchService.search(query));
    }

    /**
     * 按标题搜索
     * GET /api/search/title?title=xxx
     */
    @GetMapping("/title")
    public ApiResponse<List<PostItemVO>> searchByTitle(@RequestParam String title) {
        return ApiResponse.success(searchService.searchByTitle(title));
    }

    /**
     * 高级搜索：关键词 + 最低热度
     * GET /api/search/advanced?q=xxx&minHot=100&size=20
     */
    @GetMapping("/advanced")
    public ApiResponse<List<PostItemVO>> advancedSearch(
            @RequestParam("q") String query,
            @RequestParam(required = false) Integer minHot,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        return ApiResponse.success(searchService.advancedSearch(query, minHot, size));
    }

    /**
     * 按关键词标签查询（热度排行）
     * GET /api/search/keyword/{keyword}
     */
    @GetMapping("/keyword/{keyword}")
    public ApiResponse<List<PostItemVO>> findByKeyword(@PathVariable String keyword) {
        return ApiResponse.success(searchService.findByKeyword(keyword));
    }

    /**
     * 分页列表（按热度降序）
     * GET /api/search/list?page=0&size=10
     */
    @GetMapping("/list")
    public ApiResponse<Page<PostItemVO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(searchService.list(page, size));
    }

    /**
     * 帖子详情
     * GET /api/search/detail/{id}
     */
    @GetMapping("/detail/{id}")
    public ApiResponse<PostItemVO> detail(@PathVariable String id) {
        return ApiResponse.success(searchService.getDetail(id));
    }
}
