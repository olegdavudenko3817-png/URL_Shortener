package com.example.url_shortener.shorturl;

import com.example.url_shortener.common.exception.ShortUrlAccessDeniedException;
import com.example.url_shortener.common.exception.ShortUrlExpiredException;
import com.example.url_shortener.common.exception.ShortUrlNotFoundException;
import com.example.url_shortener.shorturl.dto.CreateShortUrlRequest;
import com.example.url_shortener.shorturl.dto.ShortUrlResponse;
import com.example.url_shortener.shorturl.dto.UpdateShortUrlRequest;
import com.example.url_shortener.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.url_shortener.shorturl.dto.ShortUrlStatisticsResponse;
import com.example.url_shortener.common.exception.AuthenticationRequiredException;
import com.example.url_shortener.common.exception.InvalidExpirationException;
import com.example.url_shortener.common.exception.InvalidUrlException;
import org.hibernate.exception.ConstraintViolationException;

import java.net.URI;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;


@RequiredArgsConstructor
@Service
@Slf4j
public class ShortUrlService {
    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String SHORT_CODE_CONSTRAINT =
            "uq_short_urls_short_code";

    private static final int CODE_LENGTH = 8;
    private static final int MAX_GENERATION_ATTEMPTS = 10;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final ShortUrlRepository shortUrlRepository;
    private final ShortUrlMapper shortUrlMapper;
    private final ShortUrlSaveService shortUrlSaveService;
    private final Clock clock;

    public ShortUrlResponse create(CreateShortUrlRequest request) {
        User currentUser = getCurrentUser();

        String originalUrl = request.getOriginalUrl();
        OffsetDateTime expiresAt = request.getExpiresAt();

        validateUrl(originalUrl);
        validateExpiration(expiresAt);

        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String shortCode = generateRandomCode();
            ShortUrl shortUrl = new ShortUrl(shortCode, originalUrl, expiresAt, currentUser);
            try {
                ShortUrl savedShortUrl = shortUrlSaveService.save(shortUrl);
                return shortUrlMapper.toResponse(savedShortUrl);
            } catch (DataIntegrityViolationException exception) {
                if (isShortCodeConflict(exception)) {
                    log.warn("Short code collision, retrying");
                    continue;
                }
                throw exception;
            }
        }
        throw new IllegalStateException("Unable to generate a unique short code");
    }

    public List<ShortUrlResponse> getAll() {
        User currentUser = getCurrentUser();
        return shortUrlRepository.findAllByUser(currentUser)
                .stream()
                .map(shortUrlMapper::toResponse)
                .toList();
    }

    public List<ShortUrlResponse> getActive() {
        User currentUser = getCurrentUser();
        OffsetDateTime now = OffsetDateTime.now(clock);
        return shortUrlRepository
                .findAllByUserAndExpiresAtAfter(currentUser, now)
                .stream()
                .map(shortUrlMapper::toResponse)
                .toList();
    }

    public ShortUrlStatisticsResponse getStatistics(Long id) {
        User currentUser = getCurrentUser();
        ShortUrl shortUrl = findById(id);
        checkOwnership(shortUrl, currentUser);
        return shortUrlMapper.toStatisticsResponse(shortUrl);
    }

    public ShortUrlResponse getById(Long id) {
        User currentUser = getCurrentUser();
        ShortUrl shortUrl = findById(id);
        checkOwnership(shortUrl, currentUser);
        return shortUrlMapper.toResponse(shortUrl);
    }

    @Transactional
    public ShortUrlResponse update(Long id, UpdateShortUrlRequest request) {
        User currentUser = getCurrentUser();

        ShortUrl shortUrl = findById(id);
        checkOwnership(shortUrl, currentUser);

        validateUrl(request.getOriginalUrl());
        validateExpiration(request.getExpiresAt());

        shortUrl.update(request.getOriginalUrl(), request.getExpiresAt());

        return shortUrlMapper.toResponse(shortUrl);
    }

    public void delete(Long id) {
        User currentUser = getCurrentUser();
        ShortUrl shortUrl = findById(id);
        checkOwnership(shortUrl, currentUser);
        shortUrlRepository.delete(shortUrl);
    }

    @Transactional
    public ResponseEntity<Void> redirect(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode).orElseThrow(() -> new ShortUrlNotFoundException("Short URL not found"));

        if (!shortUrl.getExpiresAt().isAfter(OffsetDateTime.now(clock))) {
            throw new ShortUrlExpiredException("Short URL has expired");
        }
        shortUrlRepository.incrementClickCountById(shortUrl.getId());

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(shortUrl.getOriginalUrl()))
                .build();
    }

    private ShortUrl findById(Long id) {
        return shortUrlRepository.findById(id).orElseThrow(() -> new ShortUrlNotFoundException("Short URL not found"));
    }

    private void checkOwnership(ShortUrl shortUrl, User currentUser) {
        if (!shortUrl.getUser().getId().equals(currentUser.getId())) {
            throw new ShortUrlAccessDeniedException("Access denied");
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new AuthenticationRequiredException("User is not authenticated");
        }
        return (User) authentication.getPrincipal();
    }

    private void validateUrl(String originalUrl) {
        if (originalUrl == null || originalUrl.isBlank()) {
            throw new InvalidUrlException("Invalid URL");
        }

        try {
            URI uri = URI.create(originalUrl);
            boolean validScheme = uri.getScheme() != null
                    && (uri.getScheme().equalsIgnoreCase("http")
                    || uri.getScheme().equalsIgnoreCase("https"));
            if (!validScheme || uri.getHost() == null) {
                throw new InvalidUrlException("Invalid URL");
            }
        } catch (IllegalArgumentException exception) {
            throw new InvalidUrlException("Invalid URL");
        }
    }

    private void validateExpiration(OffsetDateTime expiresAt) {
        if (expiresAt == null || !expiresAt.isAfter(OffsetDateTime.now(clock))) {
            throw new InvalidExpirationException("Expiration date must be in the future");
        }
    }

    private String generateRandomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = SECURE_RANDOM.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }
        return code.toString();
    }

    private boolean isShortCodeConflict(DataIntegrityViolationException exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolationException) {
                return SHORT_CODE_CONSTRAINT.equals(constraintViolationException.getConstraintName());
            }
            cause = cause.getCause();
        }
        return false;
    }

}