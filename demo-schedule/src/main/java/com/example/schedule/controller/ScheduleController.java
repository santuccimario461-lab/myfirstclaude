package com.example.schedule.controller;

import com.example.common.domain.model.CrawlerRequest;
import com.example.common.domain.model.CrawlerResult;
import com.example.common.dto.ApiResponse;
import com.example.spider.config.CrawlerStrategyRouter;
import com.example.spider.service.CrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 调度管理 REST API
 * 手动触发抓取 + 查看爬虫状态
 */
@Slf4j
@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final CrawlerService crawlerService;
    private final CrawlerStrategyRouter strategyRouter;

    /**
     * 手动触发抓取
     */
    @PostMapping("/crawl")
    public ApiResponse<CrawlerResult> triggerCrawl(
            @RequestParam String url,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "5") Integer maxPages,
            @RequestParam(required = false) String proxy) {

        log.info("手动触发抓取: url={}, keyword={}, maxPages={}", url, keyword, maxPages);

        CrawlerRequest request = CrawlerRequest.builder()
                .url(url)
                .keyword(keyword)
                .maxPages(maxPages)
                .proxy(proxy)
                .build();

        CrawlerResult result = crawlerService.execute(request);
        return ApiResponse.success("抓取完成", result);
    }

    /**
     * POST body 方式触发
     */
    @PostMapping("/crawl/execute")
    public ApiResponse<CrawlerResult> executeCrawl(@RequestBody CrawlerRequest request) {
        log.info("执行抓取请求: {}", request);
        CrawlerResult result = crawlerService.execute(request);
        return ApiResponse.success(result);
    }

    /**
     * 查看当前爬虫模式
     */
    @GetMapping("/mode")
    public ApiResponse<Map<String, Object>> getMode() {
        return ApiResponse.success(Map.of(
                "currentMode", strategyRouter.getCurrentMode(),
                "availableModes", strategyRouter.getAvailableModes()
        ));
    }
}
