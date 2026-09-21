package com.maxgot.shortener_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Setter
@Getter
@URL
@AllArgsConstructor
public class LinkResponse {
    private String shortCode;
    private String shortUrl;

}
