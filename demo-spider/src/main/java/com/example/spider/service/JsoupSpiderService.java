package com.example.spider.service;

import com.example.common.domain.entity.Article;
import com.example.common.domain.repository.ArticleRepository;
import com.example.spider.config.SpiderProperties;
import com.example.spider.config.SpiderProperties.CrawlTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Jsoup-based spider service implementation.
 * Crawls websites, parses content, and indexes articles into Elasticsearch.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JsoupSpiderService implements SpiderService {

    private final SpiderProperties properties;
    private final ArticleRepository articleRepository;

    @Override
    public List<Article> crawlTarget(String targetName) {
        CrawlTarget target = properties.getTargets().stream()
                .filter(t -> t.getName().equalsIgnoreCase(targetName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown spider target: " + targetName));
        return crawlTarget(target);
    }

    @Override
    public List<Article> crawlTarget(CrawlTarget target) {
        log.info("Starting crawl for target: {} -> {}", target.getName(), target.getUrl());
        List<Article> articles = new ArrayList<>();

        try {
            // Step 1: Fetch list page and extract article links
            Document listPage = fetchDocument(target.getUrl());
            Elements linkElements = listPage.select(target.getLinkSelector());

            log.info("Found {} links on target: {}", linkElements.size(), target.getName());

            int maxItems = Math.min(linkElements.size(), target.getMaxPages() * 10);

            for (int i = 0; i < maxItems; i++) {
                Element linkEl = linkElements.get(i);
                String articleUrl = linkEl.absUrl("href");
                if (articleUrl.isBlank()) {
                    continue;
                }

                try {
                    Article article = parseSingleArticle(articleUrl, target);
                    if (article != null) {
                        articles.add(article);
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse article at {}: {}", articleUrl, e.getMessage());
                }
            }

            // Step 2: Bulk index into ES
            if (!articles.isEmpty()) {
                articleRepository.saveAll(articles);
                log.info("Indexed {} articles from target: {}", articles.size(), target.getName());
            }

        } catch (IOException e) {
            log.error("Failed to crawl target {}: {}", target.getName(), e.getMessage());
        }

        return articles;
    }

    @Override
    public List<Article> crawlAllTargets() {
        log.info("Starting crawl for all {} targets", properties.getTargets().size());
        List<Article> allArticles = new ArrayList<>();
        for (CrawlTarget target : properties.getTargets()) {
            try {
                List<Article> articles = crawlTarget(target);
                allArticles.addAll(articles);
            } catch (Exception e) {
                log.error("Error crawling target {}: {}", target.getName(), e.getMessage());
            }
        }
        log.info("Total articles crawled from all targets: {}", allArticles.size());
        return allArticles;
    }

    @Override
    public Article crawlSinglePage(String url, String sourceName, String category) {
        log.info("Crawling single page: {}", url);
        try {
            Document doc = fetchDocument(url);
            String title = extractTitle(doc, null);
            String content = extractContent(doc, null);
            String author = extractAuthor(doc, null);

            Article article = Article.create(title, content, author, url, sourceName, category);
            articleRepository.save(article);
            return article;
        } catch (IOException e) {
            throw new RuntimeException("Failed to crawl page: " + url, e);
        }
    }

    // ---- internal helpers ----

    private Article parseSingleArticle(String url, CrawlTarget target) throws IOException {
        Document doc = fetchDocument(url);
        String title = extractTitle(doc, target.getTitleSelector());
        String content = extractContent(doc, target.getContentSelector());
        String author = extractAuthor(doc, target.getAuthorSelector());

        if (title.isBlank() || content.isBlank()) {
            log.debug("Skipping empty article at: {}", url);
            return null;
        }

        return Article.create(title, content, author, url, target.getName(), target.getCategory());
    }

    private Document fetchDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .timeout(properties.getTimeout())
                .maxBodySize(properties.getMaxBodySize())
                .userAgent(properties.getUserAgent())
                .followRedirects(properties.isFollowRedirects())
                .get();
    }

    private String extractTitle(Document doc, String selector) {
        if (selector != null && !selector.isBlank()) {
            Element el = doc.selectFirst(selector);
            if (el != null) return el.text();
        }
        // fallback
        Element ogTitle = doc.selectFirst("meta[property=og:title]");
        if (ogTitle != null) return ogTitle.attr("content");

        Element titleTag = doc.selectFirst("title");
        if (titleTag != null) return titleTag.text();

        Element h1 = doc.selectFirst("h1");
        if (h1 != null) return h1.text();

        return "Untitled";
    }

    private String extractContent(Document doc, String selector) {
        if (selector != null && !selector.isBlank()) {
            Element el = doc.selectFirst(selector);
            if (el != null) return cleanHtml(el.html());
        }
        // fallback: try common content containers
        String[] selectors = {"article", "[role=main]", "main", ".article-content",
                ".post-content", ".entry-content", ".content", "#content"};
        for (String s : selectors) {
            Element el = doc.selectFirst(s);
            if (el != null && el.text().length() > 100) {
                return cleanHtml(el.html());
            }
        }
        // last resort: body without noise
        Element body = doc.body();
        if (body != null) {
            body.select("script, style, nav, header, footer, aside, .sidebar, .ad").remove();
            return cleanHtml(body.html());
        }
        return "";
    }

    private String extractAuthor(Document doc, String selector) {
        if (selector != null && !selector.isBlank()) {
            Element el = doc.selectFirst(selector);
            if (el != null) return el.text();
        }
        Element meta = doc.selectFirst("meta[name=author]");
        if (meta != null) return meta.attr("content");

        String[] selectors = {".author", ".byline", "[rel=author]"};
        for (String s : selectors) {
            Element el = doc.selectFirst(s);
            if (el != null && !el.text().isBlank()) return el.text();
        }
        return "Unknown";
    }

    private String cleanHtml(String html) {
        String cleaned = Jsoup.clean(html, Safelist.relaxed());
        return Jsoup.parse(cleaned).text().replaceAll("\\s+", " ").trim();
    }
}
