package com.sqlab.shared;

import com.sqlab.domain.model.User;
import com.sqlab.domain.model.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public final class TestUsers {

    public static User user() {
        return user(TestConstants.USER_ID, "alice");
    }

    public static User user(UUID id, String username) {
        return new User(id, username, username + "@test.com", "hash", 0, UserRole.USER, LocalDateTime.now());
    }

    public static User admin() {
        return admin(TestConstants.ADMIN_ID, "admin");
    }

    public static User admin(UUID id, String username) {
        return new User(id, username, username + "@test.com", "hash", 0, UserRole.ADMIN, LocalDateTime.now());
    }

    public static User withXp(int xp) {
        return new User(TestConstants.USER_ID, "alice", "alice@test.com", "hash", xp, UserRole.USER, LocalDateTime.now());
    }

    private TestUsers() {
    }
}
