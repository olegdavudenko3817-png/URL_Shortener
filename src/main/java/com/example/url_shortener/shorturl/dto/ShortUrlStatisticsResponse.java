package com.example.url_shortener.shorturl.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrlStatisticsResponse {
    private Long id;
    private String shortCode;
    private Long clickCount;
}