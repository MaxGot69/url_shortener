package com.maxgot.shortener_service.client;

import com.maxgot.shortener_service.dto.ClickStatsResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsClientTest {

    @Mock
    private WebClient webClient;

    @InjectMocks
    private AnalyticsClient analyticsClient;

    @Test
    void getStats_whenServiceUnavailable_returnsDefaultResponse() {
        when(webClient.get())
                .thenThrow(new RuntimeException("analytics-service unavailable"));

        ClickStatsResponse response = analyticsClient.getStats("abc123");

        assertThat(response).isNotNull();
        assertThat(response.getShortCode()).isEqualTo("abc123");
        assertThat(response.getTotalClicks()).isEqualTo(0L);
        assertThat(response.getClicksToday()).isEqualTo(0L);
        assertThat(response.getClicksThisWeek()).isEqualTo(0L);
    }
}