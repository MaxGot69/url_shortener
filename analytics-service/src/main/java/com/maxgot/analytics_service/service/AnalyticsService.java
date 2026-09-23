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
        Instant clickedAt = LocalDateTime.parse(event.clickedAt())
                .toInstant(ZoneOffset.UTC);

        UUID correlationId = parseOrGenerateUuid(event.correlationId());
        ClickEvent clickEvent = new ClickEvent(
                event.shortCode(),
                event.originalUrl(),
                clickedAt,
                correlationId,
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

    private UUID parseOrGenerateUuid(String raw) {
        if (raw == null || raw.isBlank()) {
            return UUID.randomUUID();
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return UUID.randomUUID();
        }
    }
}