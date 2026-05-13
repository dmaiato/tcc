package com.sqlab.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.domain.model.Theme;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MissionDto {

    @JsonInclude(Include.NON_NULL)
    public record MissionResponse(
            UUID id,
            String title,
            String briefing,
            String objective,
            String hint,
            String ddlScript,
            String dmlScript,
            List<String> techniques,
            int xpReward,
            boolean ordered,
            Theme theme,
            DifficultyLevel difficulty,
            UUID scenarioId,
            String scenarioTitle,
            Integer scenarioOrderIndex,
            Integer scenarioTotalMissions) {}

    @JsonInclude(Include.NON_NULL)
    public record MissionSummary(
            UUID id,
            String title,
            List<String> techniques,
            int xpReward,
            boolean ordered,
            Theme theme,
            DifficultyLevel difficulty,
            UUID scenarioId) {}

    public record ValidationRequest(@NotNull List<Map<String, Object>> tuples) {}

    public record ValidationResponse(boolean correct) {}
}