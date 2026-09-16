package com.example.url_shortener.shorturl;

import com.example.url_shortener.shorturl.dto.ShortUrlResponse;
import com.example.url_shortener.shorturl.dto.ShortUrlStatisticsResponse;
import org.springframework.stereotype.Component;

@Component
public class ShortUrlMapper {
    public ShortUrlResponse toResponse(ShortUrl shortUrl) {
        return new ShortUrlResponse(
                shortUrl.getId(),
                shortUrl.getShortCode(),
                shortUrl.getOriginalUrl(),
                shortUrl.getCreatedAt(),
                shortUrl.getClickCount(),
                shortUrl.getExpiresAt()
        );
    }

    public ShortUrlStatisticsResponse toStatisticsResponse(ShortUrl shortUrl) {
        return new ShortUrlStatisticsResponse(
                shortUrl.getId(),
                shortUrl.getShortCode(),
                shortUrl.getOriginalUrl(),
                shortUrl.getCreatedAt(),
                shortUrl.getClickCount(),
                shortUrl.getExpiresAt()
        );
    }
}