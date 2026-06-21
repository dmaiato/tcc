package com.sqlab.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class Mission {

    private final UUID id;
    private String title;
    private String briefing;
    private String objective;
    private String hint;
    private String ddlScript;
    private String dmlScript;
    private List<Technique> techniques;
    private int xpReward;
    private ExpectedTuple expectedResult;
    private boolean ordered;
    private Theme theme;
    private DifficultyLevel difficulty;
    private UUID scenarioId;
    private Integer orderIndex;
    private boolean enabled;
    private int requiredLevel;

    public ValidationResult validate(List<java.util.Map<String, Object>> submitted) {
        return ordered
                ? expectedResult.matchesOrdered(submitted)
                : expectedResult.matchesUnordered(submitted);
    }

    public void disable() { this.enabled = false; }

    public void enable() { this.enabled = true; }

    public void assignToScenario(UUID scenarioId, int orderIndex) {
        this.scenarioId = scenarioId;
        this.orderIndex = orderIndex;
    }

    public void reorder(int orderIndex) { this.orderIndex = orderIndex; }
}
