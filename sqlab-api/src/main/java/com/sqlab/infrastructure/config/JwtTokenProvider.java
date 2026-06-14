package com.sqlab.infrastructure.config;

import com.sqlab.application.port.out.TokenProvider;
import com.sqlab.domain.model.UserRole;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
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
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.jwtParser = Jwts.parser().verifyWith(key).build();
    }

    @PostConstruct
    public void validateConfig() {
        if (key.getEncoded().length < 32) {
            throw new IllegalStateException("SQLAB_JWT_SECRET must be at least 32 bytes long");
        }
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

    public String extractUserId(String token) {
        return jwtParser.parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String extractRole(String token) {
        return jwtParser.parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    public boolean isValid(String token) {
        try {
            jwtParser.parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
