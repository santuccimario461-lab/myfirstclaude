package com.example.demo.infrastructure.external;

import com.example.demo.domain.service.ContentParserService;
import com.example.demo.infrastructure.common.exception.ContentParseException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Jsoup implementation of ContentParserService
 */
@Slf4j
@Service
public class JsoupContentParserService implements ContentParserService {

    @Value("${jsoup.timeout:10000}")
    private int timeout;

    @Value("${jsoup.max-body-size:0}")
    private int maxBodySize;

    @Value("${jsoup.user-agent:Mozilla/5.0}")
    private String userAgent;

    @Override
    public ParsedContent parseFromUrl(String url) {
        try {
            log.info("Fetching content from URL: {}", url);

            Document document = Jsoup.connect(url)
                    .timeout(timeout)
                    .maxBodySize(maxBodySize)
                    .userAgent(userAgent)
                    .followRedirects(true)
                    .get();

            return extractContent(document);
        } catch (IOException e) {
            log.error("Failed to fetch content from URL: {}", url, e);
            throw new ContentParseException("Failed to fetch content from URL: " + url, e);
        }
    }

    @Override
    public ParsedContent parseFromHtml(String html) {
        try {
            Document document = Jsoup.parse(html);
            return extractContent(document);
        } catch (Exception e) {
            log.error("Failed to parse HTML content", e);
            throw new ContentParseException("Failed to parse HTML content", e);
        }
    }

    private ParsedContent extractContent(Document document) {
        // Extract title
        String title = extractTitle(document);

        // Extract main content
        String content = extractMainContent(document);

        // Extract author
        String author = extractAuthor(document);

        log.debug("Extracted - Title: {}, Content length: {}, Author: {}",
                title, content.length(), author);

        return new ParsedContent(title, content, author);
    }

    private String extractTitle(Document document) {
        // Try meta og:title first
        Element ogTitle = document.selectFirst("meta[property=og:title]");
        if (ogTitle != null) {
            return ogTitle.attr("content");
        }

        // Try title tag
        Element titleTag = document.selectFirst("title");
        if (titleTag != null) {
            return titleTag.text();
        }

        // Try h1
        Element h1 = document.selectFirst("h1");
        if (h1 != null) {
            return h1.text();
        }

        return "Untitled";
    }

    private String extractMainContent(Document document) {
        // Try common content selectors
        String[] selectors = {
                "article",
                "[role=main]",
                "main",
                ".article-content",
                ".post-content",
                ".entry-content",
                ".content",
                "#content",
                ".article-body",
                ".post-body"
        };

        for (String selector : selectors) {
            Element contentElement = document.selectFirst(selector);
            if (contentElement != null && contentElement.text().length() > 100) {
                return cleanContent(contentElement.html());
            }
        }

        // Fallback: get body content
        Element body = document.body();
        if (body != null) {
            // Remove unwanted elements
            body.select("script, style, nav, header, footer, aside, .sidebar, .advertisement, .ad").remove();
            return cleanContent(body.html());
        }

        return "";
    }

    private String extractAuthor(Document document) {
        // Try meta author
        Element metaAuthor = document.selectFirst("meta[name=author]");
        if (metaAuthor != null) {
            return metaAuthor.attr("content");
        }

        // Try common author selectors
        String[] selectors = {
                ".author",
                ".byline",
                "[rel=author]",
                ".post-author",
                ".article-author"
        };

        for (String selector : selectors) {
            Element authorElement = document.selectFirst(selector);
            if (authorElement != null && !authorElement.text().isBlank()) {
                return authorElement.text();
            }
        }

        return "Unknown";
    }

    private String cleanContent(String html) {
        // Clean HTML and convert to plain text
        String cleaned = Jsoup.clean(html, Safelist.relaxed());
        Document cleanDoc = Jsoup.parse(cleaned);

        // Get text content
        String text = cleanDoc.text();

        // Normalize whitespace
        text = text.replaceAll("\\s+", " ").trim();

        return text;
    }
}
