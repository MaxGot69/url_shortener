package com.maxgot.analytics_service.controller;

import com.maxgot.analytics_service.dto.ClickStatsResponse;
import com.maxgot.analytics_service.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/{shortCode}")
    public ClickStatsResponse  getClickStats(@PathVariable String shortCode) {
        return analyticsService.getStats(shortCode);
    }

}
