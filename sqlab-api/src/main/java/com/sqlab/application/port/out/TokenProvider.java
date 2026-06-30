package com.sqlab.application.port.out;

import com.sqlab.domain.model.UserRole;

import java.util.UUID;

/**
 * Output port responsible for generating authentication tokens.
 * Decouples the use case from the concrete implementation (e.g., JWT).
 */
public interface TokenProvider {
    String generate(UUID userId, String username, UserRole role);
    UUID extractUserId(String token);
    String extractRole(String token);
    boolean isValid(String token);
}