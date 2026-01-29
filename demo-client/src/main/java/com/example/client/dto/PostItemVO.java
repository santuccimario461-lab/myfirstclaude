package com.example.client.dto;

import com.example.common.infrastructure.elasticsearch.document.PostItemDocument;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子视图对象 - 面向客户端
 */
@Data
@Builder
public class PostItemVO {

    private String id;
    private String title;
    private String postUrl;
    private Integer sortValue;
    private String rawData;
    private String keyword;
    private LocalDateTime crawlTime;

    public static PostItemVO fromDocument(PostItemDocument doc) {
        return PostItemVO.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .postUrl(doc.getPostUrl())
                .sortValue(doc.getSortValue())
                .rawData(doc.getRawData())
                .keyword(doc.getKeyword())
                .crawlTime(doc.getCrawlTime())
                .build();
    }
}
