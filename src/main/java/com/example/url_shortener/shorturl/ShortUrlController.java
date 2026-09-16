package com.example.url_shortener.shorturl;

import com.example.url_shortener.shorturl.dto.CreateShortUrlRequest;
import com.example.url_shortener.shorturl.dto.ShortUrlResponse;
import com.example.url_shortener.shorturl.dto.ShortUrlStatisticsResponse;
import com.example.url_shortener.shorturl.dto.UpdateShortUrlRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/short-urls")
@SecurityRequirement(name = "bearerAuth")
public class ShortUrlController {
    private final ShortUrlService shortUrlService;

    public ShortUrlController(ShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;
    }

    @PostMapping
    public ResponseEntity<ShortUrlResponse> create(@Valid @RequestBody CreateShortUrlRequest request) {
        ShortUrlResponse response = shortUrlService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<ShortUrlResponse> getAll() {
        return shortUrlService.getAll();
    }

    @GetMapping("/active")
    public List<ShortUrlResponse> getActive() {
        return shortUrlService.getActive();
    }

    @GetMapping("/{id}/statistics")
    public ShortUrlStatisticsResponse getStatistics(@PathVariable Long id) {
        return shortUrlService.getStatistics(id);
    }

    @GetMapping("/{id}")
    public ShortUrlResponse getById(@PathVariable Long id) {
        return shortUrlService.getById(id);
    }

    @PutMapping("/{id}")
    public ShortUrlResponse update(@PathVariable Long id, @Valid @RequestBody UpdateShortUrlRequest request) {
        return shortUrlService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        shortUrlService.delete(id);
        return ResponseEntity.noContent().build();
    }


}