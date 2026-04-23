package com.sqlab.domain.exception;

import java.util.UUID;

public class MissionNotFoundException extends RuntimeException {
    public MissionNotFoundException(UUID id) {
        super("Missão não encontrada: " + id);
    }
}