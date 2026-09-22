package com.maxgot.shortener_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClickStatsResponse {
    private String shortCode;
    private Long totalClicks;
    private Long clicksToday;
    private Long clicksThisWeek;
}
