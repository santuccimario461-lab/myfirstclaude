package com.example.demo.domain.service;

/**
 * Content Parser Domain Service Interface
 */
public interface ContentParserService {

    /**
     * Parse HTML content from URL and extract text
     */
    ParsedContent parseFromUrl(String url);

    /**
     * Parse HTML string and extract text
     */
    ParsedContent parseFromHtml(String html);

    record ParsedContent(String title, String content, String author) {}
}
