package com.sqlab.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MissionTest {

    private final UUID id = UUID.randomUUID();

    private Mission createMission(boolean ordered, ExpectedTuple expected) {
        return Mission.builder()
                .id(id)
                .title("Test Mission")
                .briefing("Briefing")
                .objective("Objective")
                .hint("Hint")
                .ddlScript("CREATE TABLE...")
                .dmlScript("INSERT...")
                .techniques(List.of(new Technique(null, "SELECT")))
                .xpReward(100)
                .expectedResult(expected)
                .ordered(ordered)
                .theme(new Theme(UUID.randomUUID(), "ASTRONOMY", null, null))
                .difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(null)
                .orderIndex(null)
                .enabled(true)
                .requiredLevel(1)
                .build();
    }

    @Test
    void validateOrderedDelegatesToMatchesOrdered() {
        var rows = List.of(Map.<String, Object>of("id", 1));
        var expected = new ExpectedTuple(rows);
        var mission = createMission(true, expected);
        var result = mission.validate(List.of(Map.of("id", 1)));
        assertTrue(result.correct());
    }

    @Test
    void validateUnorderedDelegatesToMatchesUnordered() {
        var rows = List.of(Map.<String, Object>of("id", 1));
        var expected = new ExpectedTuple(rows);
        var mission = createMission(false, expected);
        var result = mission.validate(List.of(Map.of("id", 1)));
        assertTrue(result.correct());
    }

    @Test
    void validateOrderedFailsOnWrongOrder() {
        var rows = List.of(
                Map.<String, Object>of("id", 1),
                Map.<String, Object>of("id", 2)
        );
        var expected = new ExpectedTuple(rows);
        var mission = createMission(true, expected);
        var result = mission.validate(List.of(
                Map.of("id", 2),
                Map.of("id", 1)
        ));
        assertFalse(result.correct());
    }

    @Test
    void constructorAndGetters() {
        var expected = new ExpectedTuple(List.of(Map.<String, Object>of("x", 1)));
        var mission = Mission.builder()
                .id(id)
                .title("Title")
                .briefing("Briefing")
                .objective("Obj")
                .hint("Hint")
                .ddlScript("DDL")
                .dmlScript("DML")
                .techniques(List.of(new Technique(null, "SELECT"), new Technique(null, "JOIN")))
                .xpReward(200)
                .expectedResult(expected)
                .ordered(true)
                .theme(new Theme(UUID.randomUUID(), "ASTRONOMY", null, null))
                .difficulty(DifficultyLevel.ADVANCED)
                .scenarioId(id)
                .orderIndex(1)
                .enabled(true)
                .requiredLevel(3)
                .build();

        assertEquals(id, mission.getId());
        assertEquals("Title", mission.getTitle());
        assertEquals(200, mission.getXpReward());
        assertTrue(mission.isOrdered());
        assertEquals(new Theme(UUID.randomUUID(), "ASTRONOMY", null, null), mission.getTheme());
        assertEquals(DifficultyLevel.ADVANCED, mission.getDifficulty());
        assertEquals(1, mission.getOrderIndex());
        assertTrue(mission.isEnabled());
        assertEquals(3, mission.getRequiredLevel());
    }
}
