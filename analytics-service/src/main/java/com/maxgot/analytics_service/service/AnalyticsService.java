package com.maxgot.analytics_service.service;

import com.maxgot.analytics_service.entity.ClickEvent;
import com.maxgot.analytics_service.event.LinkClickedEvent;
import com.maxgot.analytics_service.repository.ClickEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
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
}