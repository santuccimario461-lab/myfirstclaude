package com.example.common.domain.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 抓取结果
 */
@Data
public class CrawlerResult {

    private Boolean success;
    private List<PostItem> posts = new ArrayList<>();
    private Integer total;
    private String errorMessage;
    /** 执行模式 local/remote */
    private String mode;
    /** 耗时(毫秒) */
    private Long costTime;
    private LocalDateTime crawlTime;

    public CrawlerResult() {
        this.crawlTime = LocalDateTime.now();
    }

    public static CrawlerResult success(List<PostItem> posts, String mode, Long costTime) {
        CrawlerResult result = new CrawlerResult();
        result.setSuccess(true);
        result.setPosts(posts);
        result.setTotal(posts != null ? posts.size() : 0);
        result.setMode(mode);
        result.setCostTime(costTime);
        return result;
    }

    public static CrawlerResult fail(String errorMessage, String mode) {
        CrawlerResult result = new CrawlerResult();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        result.setMode(mode);
        result.setTotal(0);
        return result;
    }
}
