package com.example.url_shortener.shorturl;

import com.example.url_shortener.common.exception.*;
import com.example.url_shortener.shorturl.dto.CreateShortUrlRequest;
import com.example.url_shortener.shorturl.dto.ShortUrlResponse;
import com.example.url_shortener.shorturl.dto.ShortUrlStatisticsResponse;
import com.example.url_shortener.shorturl.dto.UpdateShortUrlRequest;
import com.example.url_shortener.user.User;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;


import java.net.URI;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ShortUrlServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-15T12:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Mock
    private ShortUrlRepository shortUrlRepository;
    @Mock
    private ShortUrlMapper shortUrlMapper;
    @Mock
    private ShortUrlSaveService shortUrlSaveService;
    @Mock
    private User currentUser;
    @Mock
    private ShortUrl shortUrl;
    private ShortUrlService shortUrlService;

    @BeforeEach
    void setUp() {
        shortUrlService = new ShortUrlService(
                shortUrlRepository,
                shortUrlMapper,
                shortUrlSaveService,
                FIXED_CLOCK
        );

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                        currentUser,
                        null,
                        Collections.emptyList()
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldGetAllShortUrlsForCurrentUser() {
        when(shortUrlRepository.findAllByUser(currentUser))
                .thenReturn(List.of(shortUrl));

        ShortUrlResponse response = mock(ShortUrlResponse.class);

        when(shortUrlMapper.toResponse(shortUrl))
                .thenReturn(response);

        List<ShortUrlResponse> result = shortUrlService.getAll();

        assertEquals(1, result.size());
        assertSame(response, result.getFirst());

        verify(shortUrlRepository).findAllByUser(currentUser);
    }

    @Test
    void shouldGetActiveShortUrlsForCurrentUser() {
        OffsetDateTime now =
                OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC);

        when(shortUrlRepository.findAllByUserAndExpiresAtAfter(
                currentUser,
                now
        )).thenReturn(List.of(shortUrl));

        ShortUrlResponse response = mock(ShortUrlResponse.class);
        when(shortUrlMapper.toResponse(shortUrl))
                .thenReturn(response);

        List<ShortUrlResponse> result = shortUrlService.getActive();

        assertEquals(1, result.size());
        assertSame(response, result.getFirst());

        verify(shortUrlRepository)
                .findAllByUserAndExpiresAtAfter(
                        currentUser,
                        now
                );
    }

    @Test
    void shouldGetShortUrlByIdForOwner() {
        when(currentUser.getId())
                .thenReturn(1L);

        when(shortUrlRepository.findById(1L))
                .thenReturn(Optional.of(shortUrl));

        when(shortUrl.getUser())
                .thenReturn(currentUser);

        ShortUrlResponse response = mock(ShortUrlResponse.class);

        when(shortUrlMapper.toResponse(shortUrl))
                .thenReturn(response);

        ShortUrlResponse result = shortUrlService.getById(1L);

        assertSame(response, result);

        verify(shortUrlRepository)
                .findById(1L);
    }

    @Test
    void shouldThrowWhenShortUrlDoesNotExist() {
        when(shortUrlRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ShortUrlNotFoundException.class, () -> shortUrlService.getById(999L));
    }

    @Test
    void shouldThrowWhenUserIsNotOwner() {
        when(currentUser.getId())
                .thenReturn(1L);

        User owner = mock(User.class);

        when(owner.getId()).thenReturn(2L);
        when(shortUrlRepository.findById(1L)).thenReturn(Optional.of(shortUrl));
        when(shortUrl.getUser()).thenReturn(owner);

        assertThrows(ShortUrlAccessDeniedException.class, () -> shortUrlService.getById(1L));
    }

    @Test
    void shouldDeleteShortUrlForOwner() {
        when(currentUser.getId()).thenReturn(1L);
        when(shortUrlRepository.findById(1L)).thenReturn(Optional.of(shortUrl));
        when(shortUrl.getUser()).thenReturn(currentUser);

        shortUrlService.delete(1L);
        verify(shortUrlRepository).delete(shortUrl);
    }

    @Test
    void shouldUpdateShortUrlForOwner() {
        when(currentUser.getId()).thenReturn(1L);

        when(shortUrlRepository.findById(1L)).thenReturn(Optional.of(shortUrl));
        when(shortUrl.getUser()).thenReturn(currentUser);

        UpdateShortUrlRequest request = new UpdateShortUrlRequest();

        request.setOriginalUrl("https://example.com");
        request.setExpiresAt(OffsetDateTime.parse("2026-10-01T12:00:00Z"));

        ShortUrlResponse response = mock(ShortUrlResponse.class);
        when(shortUrlMapper.toResponse(shortUrl))
                .thenReturn(response);

        ShortUrlResponse result = shortUrlService.update(1L, request);
        assertSame(response, result);

        verify(shortUrl).update("https://example.com",
                OffsetDateTime.parse("2026-10-01T12:00:00Z"));
    }

    @Test
    void shouldRejectInvalidUrl() {
        CreateShortUrlRequest request = new CreateShortUrlRequest();

        request.setOriginalUrl("not-a-url");
        request.setExpiresAt(OffsetDateTime.parse("2026-10-01T12:00:00Z"));

        assertThrows(InvalidUrlException.class, () -> shortUrlService.create(request));
        verifyNoInteractions(shortUrlSaveService);
    }

    @Test
    void shouldRejectExpiredUrl() {
        CreateShortUrlRequest request = new CreateShortUrlRequest();

        request.setOriginalUrl("https://example.com");
        request.setExpiresAt(OffsetDateTime.parse("2026-09-14T12:00:00Z"));

        assertThrows(InvalidExpirationException.class, () -> shortUrlService.create(request));
        verifyNoInteractions(shortUrlSaveService);
    }

    @Test
    void shouldRedirectAndIncrementClickCount() {
        when(shortUrlRepository.findByShortCode(
                "5eme5rxR"))
                .thenReturn(Optional.of(shortUrl));

        when(shortUrl.getExpiresAt())
                .thenReturn(OffsetDateTime.parse("2026-10-01T12:00:00Z"));

        when(shortUrl.getId())
                .thenReturn(1L);

        when(shortUrl.getOriginalUrl())
                .thenReturn("https://www.google.com");

        when(shortUrlRepository.incrementClickCountById(1L))
                .thenReturn(1);

        ResponseEntity<Void> result =
                shortUrlService.redirect("5eme5rxR");

        assertEquals(302, result.getStatusCode().value());
        assertEquals(URI.create("https://www.google.com"),
                result.getHeaders().getLocation()
        );

        verify(shortUrlRepository).incrementClickCountById(1L);
    }

    @Test
    void shouldThrowWhenRedirectTargetDoesNotExist() {
        when(shortUrlRepository.findByShortCode("AAAAAAAA"))
                .thenReturn(Optional.empty());

        assertThrows(ShortUrlNotFoundException.class,
                () -> shortUrlService.redirect("AAAAAAAA"));
    }

    @Test
    void shouldThrowWhenRedirectTargetExpired() {
        when(shortUrlRepository.findByShortCode(
                "5eme5rxR"
        )).thenReturn(Optional.of(shortUrl));

        when(shortUrl.getExpiresAt())
                .thenReturn(OffsetDateTime.parse("2026-09-14T12:00:00Z"));
        assertThrows(ShortUrlExpiredException.class, () -> shortUrlService.redirect("5eme5rxR"));

        verify(shortUrlRepository, never()
        ).incrementClickCountById(anyLong());
    }

    @Test
    void shouldGetStatisticsForOwner() {
        when(currentUser.getId()).thenReturn(1L);
        when(shortUrlRepository.findById(1L)).thenReturn(Optional.of(shortUrl));
        when(shortUrl.getUser()).thenReturn(currentUser);

        ShortUrlStatisticsResponse response = mock(ShortUrlStatisticsResponse.class);
        when(shortUrlMapper.toStatisticsResponse(shortUrl)).thenReturn(response);

        ShortUrlStatisticsResponse result = shortUrlService.getStatistics(1L);
        assertSame(response, result
        );
    }

    @Test
    void shouldRetryWhenShortCodeCollisionOccurs() {
        CreateShortUrlRequest request = new CreateShortUrlRequest();

        request.setOriginalUrl("https://example.com");
        request.setExpiresAt(OffsetDateTime.parse("2026-10-01T12:00:00Z"));

        ConstraintViolationException constraintViolationException = new ConstraintViolationException("duplicate key",
                        new SQLException("duplicate key"),
                        "short_urls_short_code_key");

        when(shortUrlSaveService.save(any(ShortUrl.class)))
                .thenThrow(new DataIntegrityViolationException("could not save short URL", constraintViolationException))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ShortUrlResponse response = mock(ShortUrlResponse.class);
        when(shortUrlMapper.toResponse(any(ShortUrl.class))).thenReturn(response);

        ShortUrlResponse result = shortUrlService.create(request);

        assertSame(response, result);
        verify(shortUrlSaveService, times(2)).save(any(ShortUrl.class));
    }
}