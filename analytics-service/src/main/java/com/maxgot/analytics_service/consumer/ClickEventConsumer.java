package com.maxgot.analytics_service.consumer;

import com.maxgot.analytics_service.event.LinkClickedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.maxgot.analytics_service.service.AnalyticsService;

@Component
@RequiredArgsConstructor
public class ClickEventConsumer {
    private final AnalyticsService analyticsService;
    @KafkaListener(topics = "link-clicks", containerFactory = "kafkaListenerContainerFactory")
    public void onLinkClicked(LinkClickedEvent event) {
        analyticsService.save(event);
    }
}
