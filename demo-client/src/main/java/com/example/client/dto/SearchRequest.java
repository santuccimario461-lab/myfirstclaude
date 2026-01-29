package com.example.client.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * User search request
 */
@Data
public class SearchRequest {

    @NotBlank(message = "Keyword must not be blank")
    private String keyword;

    private String category;

    private int page = 0;

    private int size = 10;
}
