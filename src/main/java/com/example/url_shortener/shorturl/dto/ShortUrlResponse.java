package com.example.url_shortener.shorturl.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrlResponse {
    private Long id;
    private String shortCode;
    private String originalUrl;
    private OffsetDateTime createdAt;
    private Long clickCount;
    private OffsetDateTime expiresAt;
}