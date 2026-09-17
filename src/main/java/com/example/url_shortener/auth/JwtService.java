package com.example.url_shortener.auth;

import com.example.url_shortener.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey secretKey;
    private final long expiration;

    public JwtService(@Value("${JWT_SECRET}") String secretKey, @Value("${JWT_EXPIRATION}") long expiration) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return parseToken(token).getSubject();
    }

    public boolean validateToken(String token, String username) {
        Claims claims = parseToken(token);
        return isTokenValid(claims, username);
    }

    public boolean isTokenValid(Claims claims, String username) {
        return username.equals(claims.getSubject()) && !claims.getExpiration().before(new Date());
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}