package com.sqlab.domain.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(java.util.UUID userId) {
        super("User not found: " + userId);
    }
}
