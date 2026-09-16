package com.example.url_shortener.shorturl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShortUrlSaveService {
    private final ShortUrlRepository shortUrlRepository;

    public ShortUrlSaveService(ShortUrlRepository shortUrlRepository) {
        this.shortUrlRepository = shortUrlRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ShortUrl save(ShortUrl shortUrl) {
        return shortUrlRepository.save(shortUrl);
    }
}
