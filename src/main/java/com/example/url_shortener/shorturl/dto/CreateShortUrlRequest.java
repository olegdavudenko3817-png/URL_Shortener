package com.example.url_shortener.shorturl.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CreateShortUrlRequest {
    @NotBlank(message = "Original URL must not be blank")
    private String originalUrl;

    @NotNull(message = "Expiration date must not be null")
    private OffsetDateTime expiresAt;
}
