package com.example.common.infrastructure.elasticsearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.common.domain.model.PostItem;
import com.example.common.infrastructure.elasticsearch.document.PostItemDocument;
import com.example.common.infrastructure.elasticsearch.repository.PostItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Elasticsearch 服务类
 * 提供帖子数据的索引、搜索、删除等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostItemEsService {

    private static final String INDEX_NAME = "post_items";

    private final PostItemRepository postItemRepository;

    @Autowired(required = false)
    private ElasticsearchClient elasticsearchClient;

    /**
     * 批量索引帖子数据（带去重）
     *
     * 去重策略：
     * 1. 输入去重 — 同批次按 postUrl 去重，保留 sortValue 最高的条目
     * 2. ID 生成   — 基于 postUrl 的 SHA-256 摘要，杜绝 hashCode 碰撞
     * 3. ES upsert — 直接 saveAll，ID 相同则覆盖（自动更新热度等字段）
     */
    public int indexPosts(List<PostItem> posts, String keyword) {
        if (posts == null || posts.isEmpty()) {
            return 0;
        }

        // 1) 输入去重：相同 postUrl 保留 sortValue 最高的
        var deduplicated = posts.stream()
                .filter(p -> p.getPostUrl() != null)
                .collect(Collectors.toMap(
                        PostItem::getPostUrl,
                        Function.identity(),
                        (a, b) -> a.getSortValue() >= b.getSortValue() ? a : b,
                        LinkedHashMap::new
                ))
                .values();

        var now = LocalDateTime.now();
        var documents = deduplicated.stream()
                .map(post -> convertToDocument(post, keyword, now))
                .toList();

        // 2) 直接 saveAll — ES 按 _id upsert，新文档插入，旧文档覆盖更新
        postItemRepository.saveAll(documents);

        int skipped = posts.size() - documents.size();
        log.info("【ES索引】upsert {} 条文档{}，关键词: {}",
                documents.size(),
                skipped > 0 ? String.format("（输入去重跳过 %d 条）", skipped) : "",
                keyword);

        return documents.size();
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
                    .toList();

        } catch (IOException e) {
            log.error("ES 高级搜索失败: {}", e.getMessage(), e);
            return List.of();
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
        doc.setId(generateId(post.getPostUrl()));
        doc.setTitle(post.getTitle());
        doc.setRawData(post.getRawData());
        doc.setSortValue(post.getSortValue());
        doc.setSourceUrl(post.getSourceUrl());
        doc.setPostUrl(post.getPostUrl());
        doc.setCrawlTime(crawlTime);
        doc.setKeyword(keyword);
        return doc;
    }

    /**
     * 基于 postUrl 生成确定性 ID（SHA-256 前 32 位 hex）
     * 相比 hashCode()：无碰撞风险，128-bit 空间
     */
    private String generateId(String postUrl) {
        if (postUrl == null) {
            return UUID.randomUUID().toString();
        }
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hash = digest.digest(postUrl.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 16);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed by the Java spec, this should never happen
            return UUID.randomUUID().toString();
        }
    }
}
