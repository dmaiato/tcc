package com.sqlab.domain.model;

public record ValidationResult(boolean correct, String feedback) {

    public static final ValidationResult CORRECT = new ValidationResult(true, null);

    public static ValidationResult failed(String feedback) {
        return new ValidationResult(false, feedback);
    }
}
