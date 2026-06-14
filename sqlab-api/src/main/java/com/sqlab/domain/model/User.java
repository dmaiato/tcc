package com.sqlab.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class User {

    private final UUID id;
    private final String username;
    private final String email;
    private final String passwordHash;
    private int xp;
    private final UserRole role;
    private final LocalDateTime createdAt;

    public void addXp(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("XP must be positive");
        this.xp += amount;
    }

    public boolean matchesPassword(String rawPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, this.passwordHash);
    }

    public static int computeLevel(int xp) {
        return (int) Math.floor(Math.sqrt(xp / 100.0)) + 1;
    }
}
