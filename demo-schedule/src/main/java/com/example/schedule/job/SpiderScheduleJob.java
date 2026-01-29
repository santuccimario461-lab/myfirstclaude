package com.example.schedule.job;

import com.example.common.domain.entity.Article;
import com.example.spider.service.SpiderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job that periodically triggers spider crawling.
 * Cron expression can be overridden via Nacos config.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpiderScheduleJob {

    private final SpiderService spiderService;

    /**
     * Default: run every 2 hours.
     * Override via config: schedule.spider.cron
     */
    @Scheduled(cron = "${schedule.spider.cron:0 0 */2 * * ?}")
    public void scheduledCrawl() {
        log.info("=== Scheduled spider crawl started at {} ===", LocalDateTime.now());
        try {
            List<Article> articles = spiderService.crawlAllTargets();
            log.info("=== Scheduled crawl completed: {} articles indexed ===", articles.size());
        } catch (Exception e) {
            log.error("=== Scheduled crawl failed ===", e);
        }
    }
}
