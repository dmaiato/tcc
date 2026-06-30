package com.sqlab.infrastructure.adapter.in.web.dto;

import com.sqlab.domain.model.DifficultyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ScenarioDto {

    private ScenarioDto() { /* this class is not meant to be intantiated */}

    public record ScenarioSummary(
            UUID id,
            String title,
            int totalMissions,
            int completedMissions,
            int requiredLevel,
            ThemeDto.ThemeResponse theme) {}

    public record ScenarioMissionItem(
            UUID id,
            String title,
            List<TechniqueDto.TechniqueResponse> techniques,
            int xpReward,
            DifficultyLevel difficulty,
            String status,
            int requiredLevel) {}

    public record ScenarioDetail(
            UUID id,
            String title,
            String description,
            List<ScenarioMissionItem> missions,
            Map<String, Integer> userProgress,
            int requiredLevel,
            ThemeDto.ThemeResponse theme) {}

    public record CreateScenarioRequest(
            @NotBlank String title,
            @NotBlank String description,
            @NotBlank String theme,
            @NotNull Boolean enabled,
            int requiredLevel) {}

    public record UpdateScenarioRequest(
            @NotBlank String title,
            @NotBlank String description,
            @NotBlank String theme,
            @NotNull Boolean enabled,
            int requiredLevel) {}

    public record ScenarioResponse(
            UUID id,
            String title,
            String description,
            int totalMissions,
            boolean enabled,
            int requiredLevel,
            ThemeDto.ThemeResponse theme) {}

    public record ScenarioMissionSummary(
            UUID id,
            String title,
            DifficultyLevel difficulty,
            int xpReward,
            boolean enabled) {}

    public record ScenarioAdminDetail(
            UUID id,
            String title,
            String description,
            boolean enabled,
            int totalMissions,
            List<ScenarioMissionSummary> missions,
            int requiredLevel,
            ThemeDto.ThemeResponse theme) {}

    public record ReorderMissionsRequest(
            @NotNull List<@NotNull UUID> missionIds) {}
}
