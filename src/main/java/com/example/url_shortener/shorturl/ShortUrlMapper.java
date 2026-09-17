package com.example.url_shortener.shorturl;

import com.example.url_shortener.shorturl.dto.ShortUrlResponse;
import com.example.url_shortener.shorturl.dto.ShortUrlStatisticsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ShortUrlMapper {
    private final String baseUrl;

    public ShortUrlMapper(@Value("${app.base-url}") String baseUrl) {
        this.baseUrl = removeTrailingSlash(baseUrl);
    }

    public ShortUrlResponse toResponse(ShortUrl shortUrl) {
        return new ShortUrlResponse(
                shortUrl.getId(),
                baseUrl + "/" + shortUrl.getShortCode(),
                shortUrl.getOriginalUrl(),
                shortUrl.getCreatedAt(),
                shortUrl.getClickCount(),
                shortUrl.getUser().getUsername(),
                shortUrl.getExpiresAt());
    }

    public ShortUrlStatisticsResponse toStatisticsResponse(ShortUrl shortUrl) {
        return new ShortUrlStatisticsResponse(
                shortUrl.getId(),
                shortUrl.getShortCode(),
                shortUrl.getClickCount());
    }

    private String removeTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}