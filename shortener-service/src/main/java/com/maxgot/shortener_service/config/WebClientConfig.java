package com.maxgot.shortener_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;
import java.time.Duration;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient webClient( @Value("${analytics.service.url}") String baseUrl) {

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(2));

        WebClient webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(baseUrl)
                .build();
            return webClient;
    }
}
