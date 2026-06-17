package com.sqlab.infrastructure.config;

import com.sqlab.domain.model.UserRole;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-that-is-at-least-32-characters-long-for-tests";
    private static final long EXPIRATION_MS = 86400000;

    private static JwtTokenProvider provider;

    @BeforeAll
    static void setUp() {
        provider = new JwtTokenProvider(SECRET, EXPIRATION_MS);
    }

    @Test
    void generate_returnsNonEmptyToken() {
        var token = provider.generate(UUID.randomUUID(), "testuser", UserRole.USER);
        assertThat(token).isNotBlank();
    }

    @Test
    void extractUserId_returnsCorrectId() {
        var userId = UUID.randomUUID();
        var token = provider.generate(userId, "testuser", UserRole.USER);
        assertThat(provider.extractUserId(token)).isEqualTo(userId.toString());
    }

    @Test
    void extractRole_returnsCorrectRole() {
        var token = provider.generate(UUID.randomUUID(), "admin", UserRole.ADMIN);
        assertThat(provider.extractRole(token)).isEqualTo("ADMIN");
    }

    @Test
    void isValid_withValidToken() {
        var token = provider.generate(UUID.randomUUID(), "user", UserRole.USER);
        assertThat(provider.isValid(token)).isTrue();
    }

    @Test
    void isValid_withMalformedToken() {
        assertThat(provider.isValid("invalid.jwt.token")).isFalse();
    }

    @Test
    void isValid_withExpiredToken() {
        var shortProvider = new JwtTokenProvider(SECRET, -1);
        var token = shortProvider.generate(UUID.randomUUID(), "user", UserRole.USER);
        assertThat(shortProvider.isValid(token)).isFalse();
    }

    @Test
    void generateWithUserRole() {
        var token = provider.generate(UUID.randomUUID(), "regular", UserRole.USER);
        assertThat(provider.extractRole(token)).isEqualTo("USER");
    }

    @Test
    void generateWithAdminRole() {
        var token = provider.generate(UUID.randomUUID(), "admin", UserRole.ADMIN);
        assertThat(provider.extractRole(token)).isEqualTo("ADMIN");
    }

    @Test
    void isValid_withEmptyString() {
        assertThat(provider.isValid("")).isFalse();
    }

    @Test
    void isValid_withNullString() {
        assertThat(provider.isValid(null)).isFalse();
    }
}
