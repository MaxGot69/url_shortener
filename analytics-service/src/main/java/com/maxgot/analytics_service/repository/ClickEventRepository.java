package com.maxgot.analytics_service.repository;

import com.maxgot.analytics_service.entity.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {
    boolean existsByCorrelationId(UUID correlationId);
}
