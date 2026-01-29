package com.example.spider.service;

import com.example.common.domain.entity.Article;
import com.example.spider.config.SpiderProperties;

import java.util.List;

/**
 * Spider service interface for crawling websites
 */
public interface SpiderService {

    /**
     * Crawl a specific target by name (as defined in spider.targets config)
     */
    List<Article> crawlTarget(String targetName);

    /**
     * Crawl a specific target config
     */
    List<Article> crawlTarget(SpiderProperties.CrawlTarget target);

    /**
     * Crawl all configured targets
     */
    List<Article> crawlAllTargets();

    /**
     * Crawl a single URL and return an article
     */
    Article crawlSinglePage(String url, String sourceName, String category);
}
