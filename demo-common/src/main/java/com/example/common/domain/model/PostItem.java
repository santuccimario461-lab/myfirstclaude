package com.example.common.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 帖子数据项 - 领域模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostItem {

    /** 帖子标题 */
    private String title;

    /** 原始数据（下载/回复数等） */
    private String rawData;

    /** 排序值（用于热度排序） */
    private int sortValue;

    /** 来源页面 URL */
    private String sourceUrl;

    /** 帖子详情 URL */
    private String postUrl;
}
