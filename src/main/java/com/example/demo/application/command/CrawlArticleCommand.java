package com.example.demo.application.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

/**
 * Command for crawling article from URL
 */
@Getter
@Builder
public class CrawlArticleCommand {

    @NotBlank(message = "URL is required")
    private String url;
}
