package com.example.admin.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Dashboard statistics for admin overview
 */
@Data
@Builder
public class DashboardStats {

    private long totalArticles;
}
