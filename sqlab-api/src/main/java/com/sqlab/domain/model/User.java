package com.sqlab.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
        if (amount <= 0) throw new IllegalArgumentException("XP deve ser um valor positivo.");
        this.xp += amount;
    }

    public static int computeLevel(int xp) {
        return (int) Math.floor(Math.sqrt(xp / 100.0)) + 1;
    }
}