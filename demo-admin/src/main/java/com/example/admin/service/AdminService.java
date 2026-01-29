package com.example.admin.service;

import com.example.admin.dto.DashboardStats;
import com.example.admin.dto.PostItemDetailDTO;
import com.example.common.infrastructure.elasticsearch.document.PostItemDocument;
import com.example.common.infrastructure.elasticsearch.service.PostItemEsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理后台服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final PostItemEsService postItemEsService;

    /**
     * 统计面板
     */
    public DashboardStats getDashboard() {
        return DashboardStats.builder()
                .totalDocuments(postItemEsService.count())
                .build();
    }

    /**
     * 分页列表（按热度降序）
     */
    public Page<PostItemDetailDTO> listAll(int page, int size) {
        return postItemEsService.findAll(page, size)
                .map(PostItemDetailDTO::fromDocument);
    }

    /**
     * 多字段搜索
     */
    public List<PostItemDetailDTO> search(String query) {
        return postItemEsService.search(query).stream()
                .map(PostItemDetailDTO::fromDocument)
                .toList();
    }

    /**
     * 按标题搜索
     */
    public List<PostItemDetailDTO> searchByTitle(String title) {
        return postItemEsService.searchByTitle(title).stream()
                .map(PostItemDetailDTO::fromDocument)
                .toList();
    }

    /**
     * 按关键词标签查询
     */
    public List<PostItemDetailDTO> findByKeyword(String keyword) {
        return postItemEsService.findByKeyword(keyword).stream()
                .map(PostItemDetailDTO::fromDocument)
                .toList();
    }

    /**
     * 高级搜索
     */
    public List<PostItemDetailDTO> advancedSearch(String query, Integer minHot, int size) {
        return postItemEsService.advancedSearch(query, minHot, size).stream()
                .map(PostItemDetailDTO::fromDocument)
                .toList();
    }

    /**
     * 删除单个帖子
     */
    public void deleteById(String id) {
        postItemEsService.deleteById(id);
        log.info("【Admin】删除帖子: {}", id);
    }

    /**
     * 按关键词删除所有帖子
     */
    public void deleteByKeyword(String keyword) {
        postItemEsService.deleteByKeyword(keyword);
        log.info("【Admin】删除关键词 [{}] 的所有帖子", keyword);
    }
}
