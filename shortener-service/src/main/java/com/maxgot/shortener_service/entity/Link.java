package com.maxgot.shortener_service.entity;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Table(name = "links")
@Entity
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String shortCode;

    @Column(nullable = false)
    private String originalUrl;

    private Long clicks;

    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Link() {}

    public Link(String shortCode, String originalUrl, Long clicks, LocalDateTime expiresAt,  LocalDateTime createdAt) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.clicks = clicks;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }
}
