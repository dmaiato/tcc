package com.sqlab.shared;

import com.sqlab.domain.model.*;

import java.util.*;

public final class TestMissions {

    public static Mission simple() {
        return builder("Simple Mission").build();
    }

    public static Mission withTechniques(String... techniqueNames) {
        return builder("Mission with Techniques")
                .techniques(Arrays.stream(techniqueNames).map(n -> new Technique(null, n)).toList())
                .build();
    }

    public static Mission inScenario(UUID scenarioId, int orderIndex) {
        return builder("Scenario Mission")
                .scenarioId(scenarioId)
                .orderIndex(orderIndex)
                .build();
    }

    public static Mission disabled() {
        return builder("Disabled Mission").enabled(false).build();
    }

    public static Mission withTheme(Theme theme) {
        return builder("Themed Mission").theme(theme).build();
    }

    public static Mission withDifficulty(DifficultyLevel difficulty) {
        return builder("Difficult Mission").difficulty(difficulty).build();
    }

    public static Mission ordered(int orderIndex) {
        return builder("Ordered Mission")
                .ordered(true)
                .orderIndex(orderIndex)
                .build();
    }

    public static Mission withResult(String column, Object value) {
        return builder("Result Mission")
                .expectedResult(new ExpectedTuple(List.of(Map.of(column, value))))
                .build();
    }

    public static Mission.MissionBuilder builder(String title) {
        return Mission.builder()
                .id(UUID.randomUUID())
                .title(title)
                .briefing("Briefing")
                .objective("Objective")
                .ddlScript("CREATE TABLE t (x INT)")
                .techniques(List.of())
                .xpReward(100)
                .expectedResult(new ExpectedTuple(List.of(Map.of("x", 1))))
                .ordered(false)
                .theme(TestThemes.astronomy())
                .difficulty(DifficultyLevel.BEGINNER)
                .enabled(true)
                .requiredLevel(0);
    }

    private TestMissions() {
    }
}
