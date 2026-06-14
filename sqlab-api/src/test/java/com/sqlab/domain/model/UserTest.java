package com.sqlab.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    private final UUID id = UUID.randomUUID();
    private final LocalDateTime now = LocalDateTime.now();

    private User createUser() {
        return new User(id, "testuser", "test@example.com", "hash123", 0, UserRole.USER, now);
    }

    @Test
    void constructorAndGetters() {
        var user = createUser();
        assertEquals(id, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("hash123", user.getPasswordHash());
        assertEquals(0, user.getXp());
        assertEquals(UserRole.USER, user.getRole());
        assertEquals(now, user.getCreatedAt());
    }

    @Test
    void addXpIncreasesXp() {
        var user = createUser();
        user.addXp(50);
        assertEquals(50, user.getXp());
    }

    @Test
    void addXpAccumulates() {
        var user = createUser();
        user.addXp(30);
        user.addXp(70);
        assertEquals(100, user.getXp());
    }

    @Test
    void addXpWithZeroThrows() {
        var user = createUser();
        assertThrows(IllegalArgumentException.class, () -> user.addXp(0));
    }

    @Test
    void addXpWithNegativeThrows() {
        var user = createUser();
        assertThrows(IllegalArgumentException.class, () -> user.addXp(-10));
    }

    @Test
    void matchesPasswordDelegatesToEncoder() {
        var user = createUser();
        when(passwordEncoder.matches("raw123", "hash123")).thenReturn(true);
        assertTrue(user.matchesPassword("raw123", passwordEncoder));
    }

    @Test
    void matchesPasswordWrongReturnsFalse() {
        var user = createUser();
        when(passwordEncoder.matches("wrong", "hash123")).thenReturn(false);
        assertFalse(user.matchesPassword("wrong", passwordEncoder));
    }

    @Test
    void computeLevelAtZeroXp() {
        assertEquals(1, User.computeLevel(0));
    }

    @Test
    void computeLevelAtThreshold() {
        assertEquals(2, User.computeLevel(100));
        assertEquals(3, User.computeLevel(400));
        assertEquals(4, User.computeLevel(900));
    }

    @Test
    void computeLevelJustBelowThreshold() {
        assertEquals(1, User.computeLevel(99));
        assertEquals(2, User.computeLevel(399));
    }
}
