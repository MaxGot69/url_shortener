package com.maxgot.analytics_service.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DlqConsumer {
    @KafkaListener(
            topics = "link-clicks-dead-letter",
            groupId = "analytics-service-dlq-group",
            containerFactory = "stringKafkaListenerContainerFactory"
    )
    public void onDeadLetter(String payload) {
        log.error("DLQ message received: {}", payload);
    }
}
