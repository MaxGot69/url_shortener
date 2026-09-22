package com.maxgot.analytics_service.service;

import com.maxgot.analytics_service.dto.ClickStatsResponse;
import com.maxgot.analytics_service.entity.ClickEvent;
import com.maxgot.analytics_service.event.LinkClickedEvent;
import com.maxgot.analytics_service.repository.ClickEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

@Service
public class AnalyticsService {
    private ClickEventRepository clickEventRepository;

    public AnalyticsService(ClickEventRepository clickEventRepository) {
        this.clickEventRepository = clickEventRepository;
    }

    @Transactional
    public void save(LinkClickedEvent event) {
        ClickEvent clickEvent = new ClickEvent(
                event.shortCode(),
                event.originalUrl(),
                event.clickedAt().toInstant(ZoneOffset.UTC),
                UUID.fromString(event.correlationId()),
                event.userAgent()
        );
        clickEventRepository.save(clickEvent);
    }

    @Transactional(readOnly = true)
    public ClickStatsResponse getStats(String shortCode) {
        Instant startOfDay = LocalDate.now(ZoneOffset.UTC)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);
        Instant startOfWeek = LocalDate.now(ZoneOffset.UTC)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);
        Long total =  clickEventRepository.countByShortCode(shortCode);
        Long today = clickEventRepository.countByShortCodeAndClickedAtAfter(shortCode, startOfDay);
        Long week = clickEventRepository.countByShortCodeAndClickedAtAfter(shortCode, startOfWeek);
        ClickStatsResponse response = new ClickStatsResponse(
                shortCode,
                total,
                today,
                week
        );
        return response;
    }
}