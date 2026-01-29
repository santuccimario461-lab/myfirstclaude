package com.example.common.domain.model;

/**
 * 抓取策略接口
 * 支持本地和远程两种实现
 */
public interface CrawlerStrategy {

    /**
     * 执行抓取
     */
    CrawlerResult crawl(CrawlerRequest request);

    /**
     * 获取策略类型: local / remote
     */
    String getType();
}
