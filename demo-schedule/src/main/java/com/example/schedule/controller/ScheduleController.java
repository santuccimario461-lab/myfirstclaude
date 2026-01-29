package com.example.schedule.controller;

import com.example.common.domain.entity.Article;
import com.example.common.dto.response.ApiResponse;
import com.example.spider.service.SpiderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints to manually trigger spider crawl jobs
 */
@Slf4j
@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final SpiderService spiderService;

    /**
     * Manually trigger crawling all configured targets
     */
    @PostMapping("/crawl/all")
    public ApiResponse<Integer> triggerCrawlAll() {
        log.info("Manual trigger: crawl all targets");
        List<Article> articles = spiderService.crawlAllTargets();
        return ApiResponse.success("Crawl completed", articles.size());
    }

    /**
     * Manually trigger crawling a specific target by name
     */
    @PostMapping("/crawl/{targetName}")
    public ApiResponse<Integer> triggerCrawlTarget(@PathVariable String targetName) {
        log.info("Manual trigger: crawl target {}", targetName);
        List<Article> articles = spiderService.crawlTarget(targetName);
        return ApiResponse.success("Crawl completed for " + targetName, articles.size());
    }

    /**
     * Crawl a single URL on demand
     */
    @PostMapping("/crawl/url")
    public ApiResponse<String> triggerCrawlUrl(
            @RequestParam String url,
            @RequestParam(defaultValue = "manual") String sourceName,
            @RequestParam(defaultValue = "general") String category) {
        log.info("Manual trigger: crawl URL {}", url);
        Article article = spiderService.crawlSinglePage(url, sourceName, category);
        return ApiResponse.success("Article crawled", article.getId().getValue());
    }
}
