package com.sqlab.domain.exception;

public class ThemeNotFoundException extends RuntimeException {
    public ThemeNotFoundException(String name) {
        super("Theme not found: " + name);
    }
}
