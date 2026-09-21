package com.maxgot.shortener_service.controller;

import com.maxgot.shortener_service.dto.CreateLinkRequest;
import com.maxgot.shortener_service.dto.LinkResponse;
import com.maxgot.shortener_service.entity.Link;
import com.maxgot.shortener_service.service.LinkService;
import com.maxgot.shortener_service.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@Valid
public class LinkController {
    @Autowired
    private LinkService linkService;
    private RateLimitService rateLimitService;

    public LinkController(LinkService linkService,  RateLimitService rateLimitService) {
        this.linkService = linkService;
        this.rateLimitService = rateLimitService;
    }


    //Создание ссылки POST /api/links
    @PostMapping("/api/links")
    public ResponseEntity<LinkResponse> create(@Valid @RequestBody CreateLinkRequest request, HttpServletRequest httpRequest) {
        //получить ip
        String ip =  httpRequest.getRemoteAddr();
        String key = "rate_limit:" + ip;

        //При 11-м запросе подряд вернуть 429
        if (rateLimitService.isRateLimited(key)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        //При первых 10 — 201 с LinkResponse
        LinkResponse created = linkService.createLink(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);

    }

    //GET /{shortCode} → редирект (302)
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> getShortCode(@PathVariable String shortCode, HttpServletRequest httpRequest) {
        String userAgent = httpRequest.getHeader("User-Agent");
        Link link = linkService.getLinkByShortCode(shortCode, userAgent);
        String originalUrl = link.getOriginalUrl();
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

    //GET /api/links/{shortCode} → информация
    @GetMapping("/api/links/{shortCode}")
    public ResponseEntity<LinkResponse> getInfo(@PathVariable String shortCode) {
        LinkResponse link = linkService.getInfoByShortCode(shortCode);
        return ResponseEntity.ok(link);
    }

    @DeleteMapping("/api/links/{shortCode}")
    public ResponseEntity<Void> delete(@PathVariable String shortCode) {
        linkService.deleteLink(shortCode);
        return ResponseEntity.noContent().build();
    }
}
