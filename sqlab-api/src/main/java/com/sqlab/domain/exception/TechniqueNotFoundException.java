package com.sqlab.domain.exception;

import java.util.UUID;

public class TechniqueNotFoundException extends RuntimeException {
    public TechniqueNotFoundException(String name) {
        super("Technique not found: " + name);
    }

    public TechniqueNotFoundException(UUID id) {
        super("Technique not found: " + id);
    }
}
