package com.maxgot.shortener_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateLinkRequest {

    @NotBlank
    @URL
    private String originalUrl;
    private LocalDateTime expiresAt;
}