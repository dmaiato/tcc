package com.sqlab.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class Mission {

    private final UUID id;
    private final String title;
    private final String briefing;
    private final String objective;
    private final String hint;
    private final String ddlScript;
    private final String dmlScript;
    private final List<String> techniques;
    private final int xpReward;
    private final ExpectedTuple expectedResult;
    private final boolean ordered;
    private final Theme theme;
    private final DifficultyLevel difficulty;
    private final UUID scenarioId;
    private final Integer orderIndex;
    private final String scenarioTitle;

    public boolean validate(List<java.util.Map<String, Object>> submitted) {
        return ordered
                ? expectedResult.matchesOrdered(submitted)
                : expectedResult.matchesUnordered(submitted);
    }
}