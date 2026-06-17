package com.sqlab.infrastructure.adapter.in.web;

import com.sqlab.domain.exception.*;
import com.sqlab.infrastructure.adapter.in.web.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleUserAlreadyExists() {
        var ex = new UserAlreadyExistsException("user exists");
        ResponseEntity<ErrorResponse> response = handler.handleUserAlreadyExists(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(Objects.requireNonNull(response.getBody()).message()).isEqualTo("user exists");
    }

    @Test
    void handleInvalidCredentials() {
        var ex = new InvalidCredentialsException();
        ResponseEntity<ErrorResponse> response = handler.handleInvalidCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void handleNotFound_mission() {
        var ex = new MissionNotFoundException(UUID.randomUUID());
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(Objects.requireNonNull(response.getBody()).message()).contains("Mission not found");
    }

    @Test
    void handleNotFound_scenario() {
        var ex = new ScenarioNotFoundException(UUID.randomUUID());
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handleValidation() {
        var ex = mock(MethodArgumentNotValidException.class);
        var bindingResult = mock(org.springframework.validation.BindingResult.class);
        var fieldError = new org.springframework.validation.FieldError("obj", "field", "must not be blank");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(Objects.requireNonNull(response.getBody()).message()).contains("field: must not be blank");
    }

    @Test
    void handleIllegalArgument() {
        var ex = new IllegalArgumentException("invalid argument");
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(Objects.requireNonNull(response.getBody()).message()).isEqualTo("invalid argument");
    }

    @Test
    void handleMessageNotReadable() {
        var ex = new HttpMessageNotReadableException("malformed", (org.springframework.http.HttpInputMessage) null);
        ResponseEntity<ErrorResponse> response = handler.handleMessageNotReadable(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(Objects.requireNonNull(response.getBody()).message()).isEqualTo("Malformed JSON request body");
    }

    @Test
    void handleMissingParam() {
        var ex = new MissingServletRequestParameterException("param", "String");
        ResponseEntity<ErrorResponse> response = handler.handleMissingParam(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleMethodNotSupported() {
        var ex = new HttpRequestMethodNotSupportedException("POST");
        ResponseEntity<ErrorResponse> response = handler.handleMethodNotSupported(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    void handleMissionLocked() {
        var scenarioId = UUID.randomUUID();
        var ex = new MissionLockedException(UUID.randomUUID(), scenarioId, "Scenario Title");
        ResponseEntity<ErrorResponse> response = handler.handleMissionLocked(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(Objects.requireNonNull(response.getBody()).code()).isEqualTo("MISSION_LOCKED");
        assertThat(response.getBody().scenarioId()).isEqualTo(scenarioId);
    }

    @Test
    void handleLevelRequired() {
        var ex = new LevelRequiredException(5, 2);
        ResponseEntity<ErrorResponse> response = handler.handleLevelRequired(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(Objects.requireNonNull(response.getBody()).code()).isEqualTo("LEVEL_REQUIRED");
        assertThat(response.getBody().requiredLevel()).isEqualTo(5);
        assertThat(response.getBody().currentLevel()).isEqualTo(2);
    }

    @Test
    void handleAccessDenied() {
        var ex = new AccessDeniedException("denied");
        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(Objects.requireNonNull(response.getBody()).message()).isEqualTo("Access denied: insufficient permissions");
    }

    @Test
    void handleThemeNotFound() {
        var ex = new ThemeNotFoundException("INVALID");
        ResponseEntity<ErrorResponse> response = handler.handleThemeNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleGeneric_returnsSanitizedMessage() {
        var ex = new RuntimeException("sensitive error details");
        ResponseEntity<ErrorResponse> response = handler.handleGeneric(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(Objects.requireNonNull(response.getBody()).message()).isEqualTo("An internal error occurred");
        assertThat(response.getBody().message()).doesNotContain("sensitive");
    }

    @Test
    void handleGeneric_includesTimestamp() {
        var ex = new RuntimeException("test");
        ResponseEntity<ErrorResponse> response = handler.handleGeneric(ex);

        assertThat(Objects.requireNonNull(response.getBody()).timestamp()).isNotNull();
    }
}
