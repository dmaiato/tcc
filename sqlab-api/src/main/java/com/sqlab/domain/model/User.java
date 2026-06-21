package com.sqlab.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class User {

    private final UUID id;
    private final String username;
    private String email;
    private String passwordHash;
    private int xp;
    private UserRole role;
    private final LocalDateTime createdAt;

    public void addXp(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("XP must be positive");
        this.xp += amount;
    }

    public static int computeLevel(int xp) {
        return (int) Math.floor(Math.sqrt(xp / 100.0)) + 1;
    }
}
