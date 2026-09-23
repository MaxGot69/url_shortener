package com.maxgot.analytics_service.config;

import com.maxgot.analytics_service.event.LinkClickedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;
@Configuration
public class KafkaConfig {

    private static final String DLQ_TOPIC = "link-clicks-dead-letter";

    @Bean
    public JacksonJsonDeserializer<LinkClickedEvent> linkClickedEventDeserializer() {
        JacksonJsonDeserializer<LinkClickedEvent> deserializer =
                new JacksonJsonDeserializer<>(LinkClickedEvent.class);
        // Producer sends __TypeId__ for shortener_service.LinkClickedEvent; ignore it and
        // always map to analytics_service.LinkClickedEvent.
        deserializer.setUseTypeHeaders(false);
        return deserializer;
    }

    @Bean
    public ConsumerFactory<String, LinkClickedEvent> linkClickedConsumerFactory(
            KafkaProperties kafkaProperties,
            JacksonJsonDeserializer<LinkClickedEvent> deserializer) {

        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        ErrorHandlingDeserializer<LinkClickedEvent> errorHandling =
                new ErrorHandlingDeserializer<>(deserializer);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                errorHandling
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, LinkClickedEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, LinkClickedEvent> linkClickedConsumerFactory,
            DefaultErrorHandler errorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, LinkClickedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(linkClickedConsumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(DLQ_TOPIC, -1)
        );
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2L));
    }

    @Bean
    public ConsumerFactory<String, String> stringConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new StringDeserializer()
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> stringKafkaListenerContainerFactory(
            ConsumerFactory<String, String> stringConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(stringConsumerFactory);
        // намеренно без commonErrorHandler — DLQ-listener только логирует
        return factory;
    }
}
