package com.sqlab.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ScenarioTest {

    private final UUID id = UUID.randomUUID();

    @Test
    void constructorAndGetters() {
        var theme = new Theme(UUID.randomUUID(), "ASTRONOMY", null, null);
        var scenario = new Scenario(id, "SQL Basics", "Learn SQL", theme, 1, true);

        assertEquals(id, scenario.getId());
        assertEquals("SQL Basics", scenario.getTitle());
        assertEquals("Learn SQL", scenario.getDescription());
        assertEquals(theme, scenario.getTheme());
        assertTrue(scenario.isEnabled());
        assertEquals(1, scenario.getRequiredLevel());
    }

    @Test
    void disabledScenario() {
        var scenario = new Scenario(id, "title", "desc", new Theme(UUID.randomUUID(), "CYBERSECURITY", null, null), 5, false);

        assertFalse(scenario.isEnabled());
        assertEquals(5, scenario.getRequiredLevel());
    }

    @Test
    void scenarioEnabled() {
        var scenario = new Scenario(id, "title", "desc", new Theme(UUID.randomUUID(), "ASTRONOMY", null, null), 1, true);
        assertTrue(scenario.isEnabled());
    }
}
