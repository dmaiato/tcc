package com.sqlab.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DomainEnumTest {

    @Test
    void difficultyLevelValues() {
        var values = DifficultyLevel.values();
        assertEquals(4, values.length);
        assertEquals(DifficultyLevel.BEGINNER, DifficultyLevel.valueOf("BEGINNER"));
        assertEquals(DifficultyLevel.INTERMEDIATE, DifficultyLevel.valueOf("INTERMEDIATE"));
        assertEquals(DifficultyLevel.ADVANCED, DifficultyLevel.valueOf("ADVANCED"));
        assertEquals(DifficultyLevel.EXPERT, DifficultyLevel.valueOf("EXPERT"));
    }

    @Test
    void themeValues() {
        var values = Theme.values();
        assertEquals(5, values.length);
        assertEquals(Theme.ASTRONOMY, Theme.valueOf("ASTRONOMY"));
        assertEquals(Theme.CYBERSECURITY, Theme.valueOf("CYBERSECURITY"));
        assertEquals(Theme.CRIMINAL, Theme.valueOf("CRIMINAL"));
        assertEquals(Theme.FINANCE, Theme.valueOf("FINANCE"));
        assertEquals(Theme.BIOLOGY, Theme.valueOf("BIOLOGY"));
    }

    @Test
    void userRoleValues() {
        var values = UserRole.values();
        assertEquals(2, values.length);
        assertEquals(UserRole.USER, UserRole.valueOf("USER"));
        assertEquals(UserRole.ADMIN, UserRole.valueOf("ADMIN"));
    }

    @Test
    void validationResultCorrect() {
        assertTrue(ValidationResult.CORRECT.correct());
        assertNull(ValidationResult.CORRECT.feedback());
    }

    @Test
    void validationResultFailed() {
        var result = ValidationResult.failed("Something went wrong");
        assertFalse(result.correct());
        assertEquals("Something went wrong", result.feedback());
    }
}
