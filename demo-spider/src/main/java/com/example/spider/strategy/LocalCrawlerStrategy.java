package com.example.spider.strategy;

import com.example.common.domain.model.CrawlerRequest;
import com.example.common.domain.model.CrawlerResult;
import com.example.common.domain.model.CrawlerStrategy;
import com.example.common.domain.model.PostItem;
import com.example.spider.scraper.T66yScraper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 本地抓取策略 - 调用 T66yScraper 直接抓取
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalCrawlerStrategy implements CrawlerStrategy {

    private final T66yScraper scraper;

    @Override
    public CrawlerResult crawl(CrawlerRequest request) {
        log.info("【本地策略】开始抓取: {}", request);
        long startTime = System.currentTimeMillis();

        try {
            List<PostItem> posts = scraper.scrape(
                    request.getUrl(),
                    request.getKeyword(),
                    request.getMaxPages(),
                    request.getProxy()
            );

            long costTime = System.currentTimeMillis() - startTime;
            log.info("【本地策略】抓取成功，耗时: {}ms，数量: {}", costTime, posts.size());
            return CrawlerResult.success(posts, getType(), costTime);

        } catch (Exception e) {
            log.error("【本地策略】抓取失败: {}", e.getMessage(), e);
            return CrawlerResult.fail(e.getMessage(), getType());
        }
    }

    @Override
    public String getType() {
        return "local";
    }
}
