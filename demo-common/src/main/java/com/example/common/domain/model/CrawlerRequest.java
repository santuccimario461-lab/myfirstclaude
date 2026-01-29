package com.example.common.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 抓取请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlerRequest {

    /** 目标URL（版块链接） */
    private String url;

    /** 关键词过滤（可选，标题必须包含该关键词） */
    private String keyword;

    /** 最大抓取页数，默认 5 */
    @Builder.Default
    private Integer maxPages = 5;

    /** 代理地址 IP:Port（可选） */
    private String proxy;

    /** 超时时间(毫秒)，默认 30000 */
    @Builder.Default
    private Integer timeout = 30000;

    public Integer getMaxPages() {
        return (maxPages != null && maxPages > 0) ? maxPages : 5;
    }

    public Integer getTimeout() {
        return timeout != null ? timeout : 30000;
    }
}
