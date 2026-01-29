package com.example.spider.controller;

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
 * Spider REST API
 * 爬虫服务的 HTTP 入口，支持手动触发抓取和查看爬虫模式
 */
@Slf4j
@RestController
@RequestMapping("/api/spider")
@RequiredArgsConstructor
public class SpiderController {

    private final CrawlerService crawlerService;
    private final CrawlerStrategyRouter strategyRouter;

    /**
     * 手动触发抓取（参数形式）
     */
    @PostMapping("/crawl")
    public ApiResponse<CrawlerResult> triggerCrawl(
            @RequestParam String url,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "5") Integer maxPages,
            @RequestParam(required = false) String proxy) {

        log.info("触发抓取: url={}, keyword={}, maxPages={}", url, keyword, maxPages);

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
     * 触发抓取（JSON Body 形式）
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
