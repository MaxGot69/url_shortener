package com.maxgot.analytics_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ClickStatsResponse {
    private String shortCode;
    private Long totalClicks;
    private Long clicksToday;
    private Long clicksThisWeek;
}
