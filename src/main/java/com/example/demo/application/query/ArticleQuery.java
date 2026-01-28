package com.example.demo.application.query;

import lombok.Builder;
import lombok.Getter;

/**
 * Query parameters for article search
 */
@Getter
@Builder
public class ArticleQuery {

    private String keyword;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 10;
}
