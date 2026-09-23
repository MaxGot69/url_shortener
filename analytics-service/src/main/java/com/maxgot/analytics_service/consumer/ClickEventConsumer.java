package com.maxgot.analytics_service.consumer;

import com.maxgot.analytics_service.event.LinkClickedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.maxgot.analytics_service.service.AnalyticsService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClickEventConsumer {
    private final AnalyticsService analyticsService;
    @KafkaListener(topics = "link-clicks", containerFactory = "kafkaListenerContainerFactory")
    public void onLinkClicked(LinkClickedEvent event) {
        log.info(">>> RECEIVED: {}", event);
        String correlationId = event.correlationId();
        if(correlationId == null || correlationId.isBlank()) {
            correlationId = "unknown";
        }
        MDC.put("correlationId", correlationId);
        try {
            analyticsService.save(event);
        } catch (Exception e) {
            log.error("Save failed for shortCode={}: {}", event.shortCode(), e.getMessage(), e);
            throw e;   // ← важно: пробрасываем дальше, чтобы DLQ сработал
        } finally {
            MDC.remove("correlationId");
        }
    }
}
