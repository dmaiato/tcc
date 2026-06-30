package com.sqlab.infrastructure.config;

import com.sqlab.application.port.out.TokenProvider;
import com.sqlab.domain.model.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider implements TokenProvider {

    private final SecretKey key;
    private final long expirationMs;
    private final JwtParser jwtParser;

    public JwtTokenProvider(
            @Value("${sqlab.jwt.secret}") String secret,
            @Value("${sqlab.jwt.expiration}") long expirationMs) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("SQLAB_JWT_SECRET must be at least 32 bytes long");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.expirationMs = expirationMs;
        this.jwtParser = Jwts.parser().verifyWith(key).build();
    }

    @Override
    public String generate(UUID userId, String username, UserRole role) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("role", role.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    private Claims claims(String token) {
        return jwtParser.parseSignedClaims(token).getPayload();
    }

    @Override
    public UUID extractUserId(String token) {
        final String userId = claims(token).getSubject();
        try {   
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid user ID format in token: " + userId);
        }
    }

    @Override
    public String extractRole(String token) {
        return claims(token).get("role", String.class);
    }

    @Override
    public boolean isValid(String token) {
        try {
            jwtParser.parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
