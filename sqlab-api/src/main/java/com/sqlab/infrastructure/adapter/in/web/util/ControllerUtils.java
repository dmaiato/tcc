package com.sqlab.infrastructure.adapter.in.web.util;

import java.util.UUID;

public class ControllerUtils {

    private ControllerUtils() {}

    public static UUID parseUserId(String userId) {
        if (userId == null) return null;
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
