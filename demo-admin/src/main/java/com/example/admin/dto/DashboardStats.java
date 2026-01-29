package com.example.admin.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 管理后台统计面板
 */
@Data
@Builder
public class DashboardStats {

    private long totalDocuments;
}
