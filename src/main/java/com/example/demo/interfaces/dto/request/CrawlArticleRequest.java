package com.example.demo.interfaces.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO for crawling an article from URL
 */
@Data
public class CrawlArticleRequest {

    @NotBlank(message = "URL is required")
    private String url;
}
