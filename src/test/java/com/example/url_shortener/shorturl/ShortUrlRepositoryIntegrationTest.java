package com.example.url_shortener.shorturl;

import com.example.url_shortener.user.User;
import com.example.url_shortener.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
        "JWT_SECRET=this-is-a-test-secret-key-with-enough-length-123456",
        "JWT_EXPIRATION=86400000"
})
class ShortUrlRepositoryIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine")
                    .withDatabaseName("url_shortener")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @Autowired
    private ShortUrlRepository shortUrlRepository;
    @Autowired
    private UserRepository userRepository;
    @Test
    @Transactional
    void shouldSaveAndFindShortUrlByShortCode() {
        User user = createUser("testuser1");
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(7);

        ShortUrl shortUrl = new ShortUrl(
                "Test1234",
                "https://example.com",
                expiresAt,
                user
        );

        shortUrlRepository.save(shortUrl);
        ShortUrl result = shortUrlRepository.findByShortCode("Test1234").orElseThrow();

        assertEquals("Test1234", result.getShortCode());
        assertEquals("https://example.com", result.getOriginalUrl());
        assertEquals(user.getId(), result.getUser().getId());

        assertEquals(0L, result.getClickCount());
    }

    @Test
    @Transactional
    void shouldFindAllShortUrlsForUser() {
        User user = createUser("testuser2");

        ShortUrl first = new ShortUrl(
                "Abc12345",
                "https://example.com/1",
                OffsetDateTime.now().plusDays(5),
                user
        );

        ShortUrl second = new ShortUrl(
                "Def67890",
                "https://example.com/2",
                OffsetDateTime.now().plusDays(10),
                user
        );

        shortUrlRepository.save(first);
        shortUrlRepository.save(second);

        List<ShortUrl> result = shortUrlRepository.findAllByUser(user);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(url -> url.getShortCode().equals("Abc12345")));
        assertTrue(result.stream().anyMatch(url -> url.getShortCode().equals("Def67890")));
    }

    @Test
    @Transactional
    void shouldFindOnlyActiveShortUrls() {
        User user = createUser("testuser3");
        OffsetDateTime now = OffsetDateTime.now();

        ShortUrl active = new ShortUrl(
                "Active01",
                "https://example.com/active",
                now.plusDays(5),
                user
        );

        ShortUrl expired = new ShortUrl(
                "Expired1",
                "https://example.com/expired",
                now.minusDays(1),
                user
        );

        shortUrlRepository.save(active);
        shortUrlRepository.save(expired);

        List<ShortUrl> result = shortUrlRepository.findAllByUserAndExpiresAtAfter(user, now);

        assertEquals(1, result.size());
        assertEquals("Active01", result.getFirst().getShortCode());
    }

    @Test
    @Transactional
    void shouldIncrementClickCountAtomically() {
        User user = createUser("testuser4");

        ShortUrl shortUrl = new ShortUrl(
                "Click123",
                "https://example.com",
                OffsetDateTime.now().plusDays(7),
                user
        );

        shortUrlRepository.saveAndFlush(shortUrl);

        assertEquals(0L, shortUrl.getClickCount());

        int firstUpdate = shortUrlRepository.incrementClickCountById(shortUrl.getId());
        int secondUpdate = shortUrlRepository.incrementClickCountById(shortUrl.getId());

        assertEquals(1, firstUpdate);
        assertEquals(1, secondUpdate);

        shortUrlRepository.flush();

        ShortUrl result = shortUrlRepository.findById(shortUrl.getId()).orElseThrow();

        assertEquals(2L, result.getClickCount());
    }

    @Test
    @Transactional
    void shouldRejectDuplicateShortCode() {
        User user = createUser("testuser5");
        ShortUrl first = new ShortUrl(
                "Unique12",
                "https://example.com/1",
                OffsetDateTime.now().plusDays(5),
                user
        );

        shortUrlRepository.saveAndFlush(first);
        ShortUrl duplicate = new ShortUrl(
                "Unique12",
                "https://example.com/2",
                OffsetDateTime.now().plusDays(5),
                user
        );
        assertThrows(DataIntegrityViolationException.class, () -> shortUrlRepository.saveAndFlush(duplicate));
    }

    private User createUser(String username) {
        User user = new User(username, "encoded-password");
        return userRepository.saveAndFlush(user);
    }
}