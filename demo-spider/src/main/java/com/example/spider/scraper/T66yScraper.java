package com.example.spider.scraper;

import com.example.common.domain.model.PostItem;
import com.example.common.infrastructure.elasticsearch.service.PostItemEsService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * T66y 论坛爬虫核心
 * 负责 HTML 解析、分页递归、关键词过滤
 */
@Slf4j
@Service
public class T66yScraper {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)");

    @Autowired
    private PostItemEsService postItemEsService;

    /**
     * 对外暴露的抓取接口
     *
     * @param url         目标主页链接
     * @param keyword     必须包含的关键词 (为空则不筛选)
     * @param maxPages    最大抓取页数
     * @param proxyString 代理地址字符串 "IP:Port"
     * @return 排序后的帖子列表
     */
    public List<PostItem> scrape(String url, String keyword, Integer maxPages, String proxyString) {
        Set<String> visitedUrls = new HashSet<>();
        List<PostItem> allPosts = new ArrayList<>();

        int pages = (maxPages != null && maxPages > 0) ? maxPages : 5;

        log.info("=== 开始抓取: URL={}, Keyword={}, MaxPages={}, Proxy={} ===", url, keyword, pages, proxyString);

        // 启动递归抓取
        crawlRecursively(url, keyword, visitedUrls, allPosts, pages, proxyString);

        // 按热度降序排序
        allPosts.sort((p1, p2) -> Integer.compare(p2.getSortValue(), p1.getSortValue()));

        // 索引到 ES
        int indexedCount = postItemEsService.indexPosts(allPosts, keyword);
        log.info("抓取完成: 共 {} 条, 索引 {} 条", allPosts.size(), indexedCount);

        return allPosts;
    }

    /**
     * 发送 GET 请求并返回 DOM 文档
     */
    public Document getPageDocument(String url, String proxyString) {
        try {
            Connection connect = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .timeout(30000);

            // 动态设置代理
            if (proxyString != null && !proxyString.trim().isEmpty()) {
                try {
                    String[] parts = proxyString.split(":");
                    if (parts.length == 2) {
                        connect.proxy(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                    } else {
                        log.warn("代理格式错误，忽略: {}", proxyString);
                    }
                } catch (NumberFormatException e) {
                    log.warn("代理端口非数字，忽略: {}", proxyString);
                }
            }

            return connect.get();
        } catch (IOException e) {
            log.error("请求失败 [{}]: {}", url, e.getMessage());
            return null;
        }
    }

    /**
     * 递归爬取逻辑
     */
    private void crawlRecursively(String url, String keyword, Set<String> visitedUrls,
                                  List<PostItem> allPosts, int maxPages, String proxyString) {
        if (visitedUrls.size() >= maxPages && !visitedUrls.contains(url)) {
            return;
        }
        if (visitedUrls.contains(url)) return;
        visitedUrls.add(url);

        log.info(">>> 正在请求: {} (代理: {})", url, proxyString == null ? "无" : proxyString);
        Document doc = getPageDocument(url, proxyString);
        if (doc == null) return;

        // 解析当前页面的帖子
        Elements postRows = doc.select("tr:has(.tal h3)");

        for (Element row : postRows) {
            try {
                // 1. 提取标题
                Element titleEl = row.selectFirst(".tal h3");
                String title = (titleEl != null) ? titleEl.text() : "未知标题";

                // 关键词过滤
                if (keyword != null && !keyword.isEmpty()) {
                    if (!title.contains(keyword)) {
                        continue;
                    }
                }

                // 2. 提取帖子链接
                String postUrl = "";
                Element linkEl = row.selectFirst(".tal h3 a");
                if (linkEl == null) linkEl = row.selectFirst(".tal a[href]");
                if (linkEl != null) postUrl = linkEl.attr("abs:href");

                // 3. 提取下载/热度数据
                Elements cells = row.select("td");
                String dataCount = "0";
                int sortValue = 0;

                if (cells.size() >= 5) {
                    dataCount = cells.get(4).text();
                    try {
                        Matcher matcher = NUMBER_PATTERN.matcher(dataCount);
                        if (matcher.find()) {
                            sortValue = Integer.parseInt(matcher.group(1));
                        }
                    } catch (Exception ignored) {
                    }
                }

                allPosts.add(new PostItem(title, dataCount, sortValue, url, postUrl));
            } catch (Exception e) {
                log.warn("解析行出错: {}", e.getMessage());
            }
        }

        // 递归分页
        if (visitedUrls.size() >= maxPages) return;

        Elements pageLinks = doc.select(".pages a");
        for (Element link : pageLinks) {
            if (visitedUrls.size() >= maxPages) break;

            String pageText = link.text().trim();
            if (pageText.matches("\\d+")) {
                String pageUrl = link.attr("abs:href");
                if (pageUrl != null && !pageUrl.isEmpty() && !visitedUrls.contains(pageUrl)) {
                    crawlRecursively(pageUrl, keyword, visitedUrls, allPosts, maxPages, proxyString);
                }
            }
        }
    }
}
