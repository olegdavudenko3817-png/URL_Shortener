package com.example.url_shortener.shorturl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedirectController {
    private final ShortUrlService shortUrlService;

    public RedirectController(ShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;
    }

    @GetMapping("/{shortCode:[A-Za-z0-9]{8}}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        return shortUrlService.redirect(shortCode);
    }
}