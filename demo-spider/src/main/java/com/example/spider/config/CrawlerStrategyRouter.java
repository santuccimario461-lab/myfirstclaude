package com.example.spider.config;

import com.example.common.domain.model.CrawlerStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 抓取策略路由器
 * 根据 Nacos 配置动态切换本地/远程策略
 */
@Slf4j
@Component
@RefreshScope
public class CrawlerStrategyRouter {

    @Value("${crawler.mode:local}")
    private String currentMode;

    private final Map<String, CrawlerStrategy> strategyMap;

    public CrawlerStrategyRouter(List<CrawlerStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(CrawlerStrategy::getType, s -> s));
        log.info("【策略路由器】已加载策略: {}", strategyMap.keySet());
    }

    public CrawlerStrategy getStrategy() {
        CrawlerStrategy strategy = strategyMap.get(currentMode);
        if (strategy == null) {
            log.error("未知的抓取模式: {}, 可用模式: {}", currentMode, strategyMap.keySet());
            throw new IllegalStateException("Unknown crawler mode: " + currentMode);
        }
        log.debug("【策略路由器】当前使用策略: {}", currentMode);
        return strategy;
    }

    public String getCurrentMode() {
        return currentMode;
    }

    public List<String> getAvailableModes() {
        return strategyMap.keySet().stream().toList();
    }
}
