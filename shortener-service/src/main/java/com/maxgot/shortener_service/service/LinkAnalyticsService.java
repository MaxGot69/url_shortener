package com.maxgot.shortener_service.service;

import com.maxgot.shortener_service.client.AnalyticsClient;
import com.maxgot.shortener_service.dto.ClickStatsResponse;
import com.maxgot.shortener_service.entity.Link;
import com.maxgot.shortener_service.exception.LinkNotFoundException;
import com.maxgot.shortener_service.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LinkAnalyticsService {
    private final AnalyticsClient analyticsClient;
    private final LinkRepository linkRepository;

    public ClickStatsResponse getStats(String shortCode) {

        Link link = linkRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new LinkNotFoundException("Link not found: " + shortCode));

        return analyticsClient.getStats(shortCode);
    }
}
