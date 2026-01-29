package com.example.admin.controller;

import com.example.admin.dto.DashboardStats;
import com.example.admin.dto.PostItemDetailDTO;
import com.example.admin.service.AdminService;
import com.example.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台接口
 * 查看所有索引的帖子、搜索、删除
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 统计面板
     * GET /api/admin/dashboard
     */
    @GetMapping("/dashboard")
    public ApiResponse<DashboardStats> dashboard() {
        return ApiResponse.success(adminService.getDashboard());
    }

    /**
     * 分页列表（按热度降序）
     * GET /api/admin/posts?page=0&size=20
     */
    @GetMapping("/posts")
    public ApiResponse<Page<PostItemDetailDTO>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(adminService.listAll(page, size));
    }

    /**
     * 多字段搜索
     * GET /api/admin/posts/search?q=关键词
     */
    @GetMapping("/posts/search")
    public ApiResponse<List<PostItemDetailDTO>> search(@RequestParam("q") String query) {
        return ApiResponse.success(adminService.search(query));
    }

    /**
     * 按标题搜索
     * GET /api/admin/posts/search/title?title=xxx
     */
    @GetMapping("/posts/search/title")
    public ApiResponse<List<PostItemDetailDTO>> searchByTitle(@RequestParam String title) {
        return ApiResponse.success(adminService.searchByTitle(title));
    }

    /**
     * 按关键词标签查询
     * GET /api/admin/posts/keyword/{keyword}
     */
    @GetMapping("/posts/keyword/{keyword}")
    public ApiResponse<List<PostItemDetailDTO>> findByKeyword(@PathVariable String keyword) {
        return ApiResponse.success(adminService.findByKeyword(keyword));
    }

    /**
     * 高级搜索
     * GET /api/admin/posts/advanced?q=xxx&minHot=100&size=20
     */
    @GetMapping("/posts/advanced")
    public ApiResponse<List<PostItemDetailDTO>> advancedSearch(
            @RequestParam("q") String query,
            @RequestParam(required = false) Integer minHot,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        return ApiResponse.success(adminService.advancedSearch(query, minHot, size));
    }

    /**
     * 删除单个帖子
     * DELETE /api/admin/posts/{id}
     */
    @DeleteMapping("/posts/{id}")
    public ApiResponse<Void> deleteById(@PathVariable String id) {
        adminService.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 按关键词删除所有帖子
     * DELETE /api/admin/posts/keyword/{keyword}
     */
    @DeleteMapping("/posts/keyword/{keyword}")
    public ApiResponse<Void> deleteByKeyword(@PathVariable String keyword) {
        adminService.deleteByKeyword(keyword);
        return ApiResponse.success("已删除关键词 [" + keyword + "] 的所有数据", null);
    }
}
