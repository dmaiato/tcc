package com.sqlab.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class Mission {

    private final UUID id;
    private final String title;
    private final String briefing;
    private final String ddlScript;
    private final String dmlScript;
    private final List<String> techniques;
    private final int xpReward;
    private final ExpectedTuple expectedResult;
    private final boolean ordered;
    private final Theme theme;
    private final DifficultyLevel difficulty;

    public boolean validate(List<java.util.Map<String, Object>> submitted) {
        return ordered
                ? expectedResult.matchesOrdered(submitted)
                : expectedResult.matchesUnordered(submitted);
    }
}