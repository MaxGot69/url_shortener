package com.maxgot.analytics_service.event;

import java.time.LocalDateTime;

    public record LinkClickedEvent(String shortCode,
                                   String originalUrl,
                                   String clickedAt,
                                   String userAgent,
                                   String correlationId) {
    }
