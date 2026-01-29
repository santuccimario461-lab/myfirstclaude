package com.example.common.infrastructure.elasticsearch.repository;

import com.example.common.infrastructure.elasticsearch.document.PostItemDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ES 帖子文档 Repository
 */
@Repository
public interface PostItemRepository extends ElasticsearchRepository<PostItemDocument, String> {

    /** 根据标题搜索（模糊匹配） */
    List<PostItemDocument> findByTitleContaining(String title);

    /** 根据关键词标签查询 */
    List<PostItemDocument> findByKeyword(String keyword);

    /** 根据关键词标签查询，按热度降序 */
    List<PostItemDocument> findByKeywordOrderBySortValueDesc(String keyword);

    /** 分页查询所有，按热度降序 */
    Page<PostItemDocument> findAllByOrderBySortValueDesc(Pageable pageable);

    /** 根据 postUrl 判断是否已存在（去重用） */
    boolean existsByPostUrl(String postUrl);

    /** 根据 postUrl 查找 */
    PostItemDocument findByPostUrl(String postUrl);

    /** 自定义 DSL 查询 - 多字段搜索 */
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^3\", \"keyword\"]}}")
    List<PostItemDocument> searchByMultiFields(String keyword);

    /** 删除指定关键词的所有文档 */
    void deleteByKeyword(String keyword);
}
