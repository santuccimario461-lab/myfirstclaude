package com.example.spider.service;

import com.example.common.domain.model.CrawlerRequest;
import com.example.common.domain.model.CrawlerResult;
import com.example.common.domain.model.CrawlerStrategy;
import com.example.spider.config.CrawlerStrategyRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 抓取应用服务
 * 通过 CrawlerStrategyRouter 获取当前策略，动态切换 local/remote
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlerService {

    private final CrawlerStrategyRouter strategyRouter;

    /**
     * 执行抓取
     */
    public CrawlerResult execute(CrawlerRequest request) {
        String currentMode = strategyRouter.getCurrentMode();
        log.info("【CrawlerService】开始执行，当前模式: {}, 请求: {}", currentMode, request);

        CrawlerStrategy strategy = strategyRouter.getStrategy();
        CrawlerResult result = strategy.crawl(request);

        log.info("【CrawlerService】执行完成，模式: {}, 成功: {}, 数量: {}, 耗时: {}ms",
                result.getMode(), result.getSuccess(), result.getTotal(), result.getCostTime());

        return result;
    }
}
