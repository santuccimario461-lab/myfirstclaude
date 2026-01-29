package com.example.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated response wrapper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> items;
    private int page;
    private int size;
    private long total;

    public static <T> PageResponse<T> of(List<T> items, int page, int size, long total) {
        return PageResponse.<T>builder()
                .items(items)
                .page(page)
                .size(size)
                .total(total)
                .build();
    }
}
