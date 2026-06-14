package com.sqlab.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ScenarioTest {

    private final UUID id = UUID.randomUUID();

    @Test
    void constructorAndGetters() {
        var scenario = new Scenario(id, "SQL Basics", "Learn SQL", Theme.ASTRONOMY, true, 1);

        assertEquals(id, scenario.getId());
        assertEquals("SQL Basics", scenario.getTitle());
        assertEquals("Learn SQL", scenario.getDescription());
        assertEquals(Theme.ASTRONOMY, scenario.getTheme());
        assertTrue(scenario.isEnabled());
        assertEquals(1, scenario.getRequiredLevel());
    }

    @Test
    void disabledScenario() {
        var scenario = new Scenario(id, "title", "desc", Theme.CYBERSECURITY, false, 5);

        assertFalse(scenario.isEnabled());
        assertEquals(5, scenario.getRequiredLevel());
    }

    @Test
    void scenarioEnabled() {
        var scenario = new Scenario(id, "title", "desc", Theme.ASTRONOMY, true, 1);
        assertTrue(scenario.isEnabled());
    }
}
