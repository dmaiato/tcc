package com.sqlab.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ScenarioTest {

    private final UUID id = UUID.randomUUID();

    @Test
    void constructorAndGetters() {
        var scenario = new Scenario(id, "SQL Basics", "Learn SQL", new Theme(UUID.randomUUID(), "ASTRONOMY", null, null), true, 1);

        assertEquals(id, scenario.getId());
        assertEquals("SQL Basics", scenario.getTitle());
        assertEquals("Learn SQL", scenario.getDescription());
        assertEquals(new Theme(UUID.randomUUID(), "ASTRONOMY", null, null), scenario.getTheme());
        assertTrue(scenario.isEnabled());
        assertEquals(1, scenario.getRequiredLevel());
    }

    @Test
    void disabledScenario() {
        var scenario = new Scenario(id, "title", "desc", new Theme(UUID.randomUUID(), "CYBERSECURITY", null, null), false, 5);

        assertFalse(scenario.isEnabled());
        assertEquals(5, scenario.getRequiredLevel());
    }

    @Test
    void scenarioEnabled() {
        var scenario = new Scenario(id, "title", "desc", new Theme(UUID.randomUUID(), "ASTRONOMY", null, null), true, 1);
        assertTrue(scenario.isEnabled());
    }
}
