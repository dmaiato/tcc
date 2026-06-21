package com.sqlab.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sqlab.domain.model.DifficultyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MissionDto {

    public record UpsertMissionRequest(
            @NotBlank String title,
            @NotBlank String briefing,
            @NotBlank String objective,
            String hint,
            @NotBlank String ddlScript,
            String dmlScript,
            List<String> techniques,
            int xpReward,
            boolean ordered,
            @NotBlank String theme,
            @NotNull DifficultyLevel difficulty,
            @NotEmpty List<Map<String, Object>> expectedResult,
            UUID scenarioId,
            Integer orderIndex,
            Boolean enabled) {}

    @JsonInclude(Include.NON_NULL)
    public record MissionResponse(
            UUID id,
            String title,
            String briefing,
            String objective,
            String hint,
            String ddlScript,
            String dmlScript,
            List<TechniqueDto.TechniqueResponse> techniques,
            int xpReward,
            boolean ordered,
            DifficultyLevel difficulty,
            UUID scenarioId,
            String scenarioTitle,
            Integer scenarioOrderIndex,
            Integer scenarioTotalMissions,
            boolean enabled,
            List<Map<String, Object>> expectedResult,
            int requiredLevel,
            ThemeDto.ThemeResponse theme) {}

    @JsonInclude(Include.NON_NULL)
    public record MissionSummary(
            UUID id,
            String title,
            String scenarioTitle,
            List<TechniqueDto.TechniqueResponse> techniques,
            int xpReward,
            boolean ordered,
            DifficultyLevel difficulty,
            UUID scenarioId,
            boolean enabled,
            int requiredLevel,
            ThemeDto.ThemeResponse theme) {}

    public record ValidationRequest(@NotNull List<Map<String, Object>> tuples) {}

    @JsonInclude(Include.NON_NULL)
    public record ValidationResponse(boolean correct, String feedback) {}
}
