package com.example.common.infrastructure.elasticsearch.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;

/**
 * ES 帖子文档实体
 * 索引名: post_items
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "post_items")
@Setting(shards = 1, replicas = 0)
public class PostItemDocument {

    @Id
    private String id;

    /** 帖子标题 - 使用 ik 分词器支持中文搜索 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    /** 原始数据（下载/回复数等） */
    @Field(type = FieldType.Keyword)
    private String rawData;

    /** 排序值（用于热度排序） */
    @Field(type = FieldType.Integer)
    private Integer sortValue;

    /** 来源页面 URL */
    @Field(type = FieldType.Keyword)
    private String sourceUrl;

    /** 帖子详情 URL */
    @Field(type = FieldType.Keyword)
    private String postUrl;

    /** 抓取时间 */
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd HH:mm:ss||uuuu-MM-dd||epoch_millis")
    private LocalDateTime crawlTime;

    /** 关键词标签 */
    @Field(type = FieldType.Keyword)
    private String keyword;
}
