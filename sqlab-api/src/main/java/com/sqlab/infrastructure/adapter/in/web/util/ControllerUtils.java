package com.sqlab.infrastructure.adapter.in.web.util;

import java.util.UUID;

public class ControllerUtils {

    private ControllerUtils() {}

    public static UUID parseUserId(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID not found in token");
        }
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid user ID format in token: " + userId);
        }
    }
}
