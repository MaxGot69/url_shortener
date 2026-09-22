package com.maxgot.shortener_service.client;

import com.maxgot.shortener_service.dto.ClickStatsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsClient {
    private final WebClient webClient;
    private ClickStatsResponse clickStatsResponse;

    public ClickStatsResponse getStats(String shortCode) {
        try {
            ClickStatsResponse result = webClient.get()
                    .uri("/api/analytics/{shortCode}", shortCode)
                    .retrieve()
                    .bodyToMono(ClickStatsResponse.class)
                    .block();
            return result;
        } catch (Exception e) {
            log.warn("Failed to fetch analytics for shortCode={}: {}", shortCode, e.getMessage());
            ClickStatsResponse fallback = new ClickStatsResponse(shortCode,0L,0L,0L);

            return fallback;
        }
    }
}
