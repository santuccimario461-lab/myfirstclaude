package com.example.common.infrastructure.elasticsearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.common.domain.model.PostItem;
import com.example.common.infrastructure.elasticsearch.document.PostItemDocument;
import com.example.common.infrastructure.elasticsearch.repository.PostItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Elasticsearch 服务类
 * 提供帖子数据的索引、搜索、删除等功能
 */
@Slf4j
@Service
public class PostItemEsService {

    private static final String INDEX_NAME = "post_items";

    @Autowired
    private PostItemRepository postItemRepository;

    @Autowired(required = false)
    private ElasticsearchClient elasticsearchClient;

    /**
     * 批量索引帖子数据（带去重）
     */
    public int indexPosts(List<PostItem> posts, String keyword) {
        if (posts == null || posts.isEmpty()) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        List<PostItemDocument> candidates = posts.stream()
                .map(post -> convertToDocument(post, keyword, now))
                .collect(Collectors.toList());

        List<String> candidateIds = candidates.stream()
                .map(PostItemDocument::getId)
                .collect(Collectors.toList());

        // 批量查询已存在的 ID
        Iterable<PostItemDocument> existingDocs = postItemRepository.findAllById(candidateIds);
        Set<String> existingIds = StreamSupport.stream(existingDocs.spliterator(), false)
                .map(PostItemDocument::getId)
                .collect(Collectors.toSet());

        // 过滤已存在的文档
        List<PostItemDocument> newDocuments = new ArrayList<>();
        for (PostItemDocument doc : candidates) {
            if (existingIds.contains(doc.getId())) {
                log.debug("帖子已存在，跳过: {}", doc.getPostUrl());
                continue;
            }
            newDocuments.add(doc);
        }

        if (!newDocuments.isEmpty()) {
            postItemRepository.saveAll(newDocuments);
            log.info("【ES索引】成功索引 {} 条新帖子（跳过 {} 条），关键词: {}",
                    newDocuments.size(), posts.size() - newDocuments.size(), keyword);
        }

        return newDocuments.size();
    }

    /**
     * 多字段搜索
     */
    public List<PostItemDocument> search(String keyword) {
        return postItemRepository.searchByMultiFields(keyword);
    }

    /**
     * 根据标题搜索
     */
    public List<PostItemDocument> searchByTitle(String title) {
        return postItemRepository.findByTitleContaining(title);
    }

    /**
     * 根据关键词标签查询（按热度降序）
     */
    public List<PostItemDocument> findByKeyword(String keyword) {
        return postItemRepository.findByKeywordOrderBySortValueDesc(keyword);
    }

    /**
     * 分页查询（按热度降序）
     */
    public Page<PostItemDocument> findAll(int page, int size) {
        return postItemRepository.findAllByOrderBySortValueDesc(PageRequest.of(page, size));
    }

    /**
     * 高级搜索 - 使用 ES Client 直接查询
     * 支持 title 匹配 + sortValue >= minHot 过滤
     */
    public List<PostItemDocument> advancedSearch(String keyword, Integer minSortValue, int size) {
        if (elasticsearchClient == null) {
            log.warn("ElasticsearchClient 未配置，降级使用 Repository 查询");
            return postItemRepository.searchByMultiFields(keyword);
        }

        try {
            SearchRequest request = SearchRequest.of(s -> s
                    .index(INDEX_NAME)
                    .query(q -> q
                            .bool(b -> {
                                b.must(m -> m
                                        .match(mt -> mt
                                                .field("title")
                                                .query(keyword)
                                        )
                                );
                                if (minSortValue != null) {
                                    b.filter(f -> f
                                            .range(r -> r
                                                    .field("sortValue")
                                                    .gte(co.elastic.clients.json.JsonData.of(minSortValue))
                                            )
                                    );
                                }
                                return b;
                            })
                    )
                    .sort(so -> so.field(f -> f.field("sortValue").order(SortOrder.Desc)))
                    .size(size)
            );

            SearchResponse<PostItemDocument> response = elasticsearchClient.search(request, PostItemDocument.class);
            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            log.error("ES 高级搜索失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据关键词删除所有相关文档
     */
    public void deleteByKeyword(String keyword) {
        postItemRepository.deleteByKeyword(keyword);
        log.info("【ES删除】已删除关键词 [{}] 的所有文档", keyword);
    }

    /**
     * 根据 ID 删除
     */
    public void deleteById(String id) {
        postItemRepository.deleteById(id);
    }

    /**
     * 统计文档总数
     */
    public long count() {
        return postItemRepository.count();
    }

    /**
     * 转换 PostItem -> PostItemDocument
     */
    private PostItemDocument convertToDocument(PostItem post, String keyword, LocalDateTime crawlTime) {
        PostItemDocument doc = new PostItemDocument();
        doc.setId(post.getPostUrl() != null ? String.valueOf(post.getPostUrl().hashCode()) : UUID.randomUUID().toString());
        doc.setTitle(post.getTitle());
        doc.setRawData(post.getRawData());
        doc.setSortValue(post.getSortValue());
        doc.setSourceUrl(post.getSourceUrl());
        doc.setPostUrl(post.getPostUrl());
        doc.setCrawlTime(crawlTime);
        doc.setKeyword(keyword);
        return doc;
    }
}
