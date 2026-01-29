package com.example.admin.dto;

import com.example.common.infrastructure.elasticsearch.document.PostItemDocument;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子完整详情 - 管理后台
 */
@Data
@Builder
public class PostItemDetailDTO {

    private String id;
    private String title;
    private String rawData;
    private Integer sortValue;
    private String sourceUrl;
    private String postUrl;
    private LocalDateTime crawlTime;
    private String keyword;

    public static PostItemDetailDTO fromDocument(PostItemDocument doc) {
        return PostItemDetailDTO.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .rawData(doc.getRawData())
                .sortValue(doc.getSortValue())
                .sourceUrl(doc.getSourceUrl())
                .postUrl(doc.getPostUrl())
                .crawlTime(doc.getCrawlTime())
                .keyword(doc.getKeyword())
                .build();
    }
}
