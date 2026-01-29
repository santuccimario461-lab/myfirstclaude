package com.example.spider.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Spider configuration properties loaded from Nacos / application.yml
 */
@Data
@Component
@ConfigurationProperties(prefix = "spider")
public class SpiderProperties {

    private int timeout = 10000;
    private int maxBodySize = 0;
    private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private boolean followRedirects = true;
    private int maxRetries = 3;

    /**
     * Configured crawl targets
     */
    private List<CrawlTarget> targets = new ArrayList<>();

    @Data
    public static class CrawlTarget {
        private String name;
        private String url;
        private String category;
        private String listSelector;
        private String linkSelector;
        private String titleSelector;
        private String contentSelector;
        private String authorSelector;
        private int maxPages = 1;
    }
}
