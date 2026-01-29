package com.example.schedule.job;

import com.example.common.domain.model.CrawlerRequest;
import com.example.common.domain.model.CrawlerResult;
import com.example.spider.service.CrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 定时爬虫调度任务
 * cron 表达式可通过 Nacos 配置热更新
 */
@Slf4j
@Component
@RefreshScope
@RequiredArgsConstructor
public class SpiderScheduleJob {

    private final CrawlerService crawlerService;

    /** 默认抓取目标 URL，可通过 Nacos 配置 */
    @Value("${schedule.spider.target-url:}")
    private String targetUrl;

    /** 默认关键词，可通过 Nacos 配置 */
    @Value("${schedule.spider.keyword:}")
    private String keyword;

    /** 最大抓取页数 */
    @Value("${schedule.spider.max-pages:5}")
    private Integer maxPages;

    /** 代理地址 */
    @Value("${schedule.spider.proxy:}")
    private String proxy;

    /**
     * 定时抓取任务，默认每 2 小时执行
     * 可通过 Nacos 配置 schedule.spider.cron 覆盖
     */
    @Scheduled(cron = "${schedule.spider.cron:0 0 */2 * * ?}")
    public void scheduledCrawl() {
        if (targetUrl == null || targetUrl.isBlank()) {
            log.info("=== 定时任务跳过: 未配置 schedule.spider.target-url ===");
            return;
        }

        log.info("=== 定时爬取开始 {} ===", LocalDateTime.now());
        try {
            CrawlerRequest request = CrawlerRequest.builder()
                    .url(targetUrl)
                    .keyword(keyword != null && !keyword.isBlank() ? keyword : null)
                    .maxPages(maxPages)
                    .proxy(proxy != null && !proxy.isBlank() ? proxy : null)
                    .build();

            CrawlerResult result = crawlerService.execute(request);

            if (result.getSuccess()) {
                log.info("=== 定时爬取完成: {} 条帖子, 耗时 {}ms ===", result.getTotal(), result.getCostTime());
            } else {
                log.error("=== 定时爬取失败: {} ===", result.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("=== 定时爬取异常 ===", e);
        }
    }
}
