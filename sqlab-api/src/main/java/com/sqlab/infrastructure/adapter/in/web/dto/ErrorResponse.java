package com.sqlab.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(Include.NON_NULL)
public record ErrorResponse(int status, String message, LocalDateTime timestamp,
                            String code, UUID scenarioId,
                            Integer requiredLevel, Integer currentLevel) {

    public ErrorResponse(int status, String message) {
        this(status, message, LocalDateTime.now(), null, null, null, null);
    }

    public ErrorResponse(int status, String message, String code) {
        this(status, message, LocalDateTime.now(), code, null, null, null);
    }

    public ErrorResponse(int status, String message, String code, UUID scenarioId) {
        this(status, message, LocalDateTime.now(), code, scenarioId, null, null);
    }

    public ErrorResponse(int status, String message, String code, int requiredLevel, int currentLevel) {
        this(status, message, LocalDateTime.now(), code, null, requiredLevel, currentLevel);
    }
}