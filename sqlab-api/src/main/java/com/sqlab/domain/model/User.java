package com.sqlab.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
public class User {

    private final UUID id;
    private final String username;
    private final String email;
    private final String passwordHash;
    private int xp;
    private final UserRole role;

    public void addXp(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("XP deve ser um valor positivo.");
        this.xp += amount;
    }
}