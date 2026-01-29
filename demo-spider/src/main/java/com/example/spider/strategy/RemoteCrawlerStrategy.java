package com.example.spider.strategy;

import com.example.common.domain.model.CrawlerRequest;
import com.example.common.domain.model.CrawlerResult;
import com.example.common.domain.model.CrawlerStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 远程抓取策略 - 通过 HTTP 调用远程抓取服务
 */
@Slf4j
@Component
@RefreshScope
@RequiredArgsConstructor
public class RemoteCrawlerStrategy implements CrawlerStrategy {

    private final RestTemplate restTemplate;

    @Value("${crawler.remote.url:http://localhost:8084/api/spider/crawl/execute}")
    private String remoteUrl;

    @Override
    public CrawlerResult crawl(CrawlerRequest request) {
        log.info("【远程模式】调用远程服务: {}, 请求: {}", remoteUrl, request);
        long startTime = System.currentTimeMillis();

        try {
            ResponseEntity<CrawlerResult> response = restTemplate.postForEntity(
                    remoteUrl, request, CrawlerResult.class);

            CrawlerResult result = response.getBody();
            long costTime = System.currentTimeMillis() - startTime;

            if (result != null) {
                result.setMode(getType());
                result.setCostTime(costTime);
                log.info("【远程模式】调用成功，耗时: {}ms, 数量: {}", costTime, result.getTotal());
            }

            return result;
        } catch (Exception e) {
            log.error("【远程模式】调用失败: {}", e.getMessage(), e);
            return CrawlerResult.fail("远程调用失败: " + e.getMessage(), getType());
        }
    }

    @Override
    public String getType() {
        return "remote";
    }
}
