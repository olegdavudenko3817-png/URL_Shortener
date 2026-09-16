package com.example.url_shortener.auth;

import com.example.url_shortener.user.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtServiceTest {
    private static final String SECRET = "this-is-a-very-long-secret-key-for-jwt-testing-123456789";
    private static final long EXPIRATION = 86_400_000L;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION);
    }

    @Test
    void shouldGenerateToken() {
        User user = mock(User.class);

        when(user.getUsername()).thenReturn("testuser");
        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldExtractUsernameFromToken() {
        User user = mock(User.class);

        when(user.getUsername()).thenReturn("testuser");

        String token = jwtService.generateToken(user);
        String username = jwtService.extractUsername(token);

        assertEquals("testuser", username);
    }

    @Test
    void shouldValidateValidToken() {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("testuser");

        String token = jwtService.generateToken(user);
        boolean result = jwtService.validateToken(token, "testuser");

        assertTrue(result);
    }

    @Test
    void shouldRejectTokenForDifferentUsername() {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("testuser");

        String token = jwtService.generateToken(user);
        boolean result = jwtService.validateToken(token, "another-user");

        assertFalse(result);
    }

    @Test
    void shouldRejectExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder().subject("testuser")
                        .issuedAt(Date.from(
                                Instant.now().minusSeconds(3600)))
                        .expiration(Date.from(
                                Instant.now().minusSeconds(1800)))
                        .signWith(key)
                        .compact();
        assertThrows(Exception.class, () -> jwtService.validateToken(expiredToken, "testuser"));
    }

    @Test
    void shouldRejectInvalidToken() {
        assertThrows(Exception.class, () -> jwtService.extractUsername("invalid-token"));
    }
}