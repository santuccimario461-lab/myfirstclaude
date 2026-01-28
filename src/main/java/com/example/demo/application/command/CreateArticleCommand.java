package com.example.demo.application.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

/**
 * Command for creating an article
 */
@Getter
@Builder
public class CreateArticleCommand {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    private String author;

    private String sourceUrl;
}
