package com.example.client.service;

import com.example.client.dto.PostItemVO;
import com.example.common.infrastructure.elasticsearch.document.PostItemDocument;
import com.example.common.infrastructure.elasticsearch.service.PostItemEsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 客户端搜索服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final PostItemEsService postItemEsService;

    /**
     * 多字段关键词搜索
     */
    public List<PostItemVO> search(String query) {
        log.info("用户搜索: {}", query);
        return postItemEsService.search(query).stream()
                .map(PostItemVO::fromDocument)
                .toList();
    }

    /**
     * 按标题搜索
     */
    public List<PostItemVO> searchByTitle(String title) {
        return postItemEsService.searchByTitle(title).stream()
                .map(PostItemVO::fromDocument)
                .toList();
    }

    /**
     * 高级搜索：关键词 + 最低热度过滤
     */
    public List<PostItemVO> advancedSearch(String query, Integer minHot, int size) {
        return postItemEsService.advancedSearch(query, minHot, size).stream()
                .map(PostItemVO::fromDocument)
                .toList();
    }

    /**
     * 按关键词标签查询（热度排行）
     */
    public List<PostItemVO> findByKeyword(String keyword) {
        return postItemEsService.findByKeyword(keyword).stream()
                .map(PostItemVO::fromDocument)
                .toList();
    }

    /**
     * 分页列表（按热度降序）
     */
    public Page<PostItemVO> list(int page, int size) {
        return postItemEsService.findAll(page, size)
                .map(PostItemVO::fromDocument);
    }

    /**
     * 获取帖子详情
     */
    public PostItemVO getDetail(String id) {
        PostItemDocument doc = postItemEsService.findAll(0, 1000).stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("帖子不存在: " + id));
        return PostItemVO.fromDocument(doc);
    }
}
