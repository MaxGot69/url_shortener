package com.maxgot.analytics_service.service;

import com.maxgot.analytics_service.entity.ClickEvent;
import com.maxgot.analytics_service.event.LinkClickedEvent;
import com.maxgot.analytics_service.repository.ClickEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private ClickEventRepository clickEventRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void save_savesEventToRepository() {
        LinkClickedEvent event = buildEvent("abc123");

        analyticsService.save(event);

        ArgumentCaptor<ClickEvent> captor = ArgumentCaptor.forClass(ClickEvent.class);
        verify(clickEventRepository, times(1)).save(captor.capture());

        ClickEvent saved = captor.getValue();
        assertThat(saved.getShortCode()).isEqualTo("abc123");
        assertThat(saved.getOriginalUrl()).isEqualTo("https://example.com");
        assertThat(saved.getCorrelationId()).isEqualTo(UUID.fromString(event.correlationId()));
    }

    @Test
    void save_twoEventsWithSameShortCode_savesBoth() {
        LinkClickedEvent first = buildEvent("abc123");
        LinkClickedEvent second = buildEvent("abc123");

        analyticsService.save(first);
        analyticsService.save(second);

        verify(clickEventRepository, times(2)).save(any(ClickEvent.class));
    }

    private LinkClickedEvent buildEvent(String shortCode) {
        return new LinkClickedEvent(
                shortCode,
                "https://example.com",
                LocalDateTime.now(),
                "Mozilla/5.0",
                UUID.randomUUID().toString()
        );
    }
}