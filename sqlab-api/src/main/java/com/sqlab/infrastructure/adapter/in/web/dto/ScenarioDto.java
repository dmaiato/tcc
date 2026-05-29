package com.sqlab.infrastructure.adapter.in.web.dto;

import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.domain.model.Theme;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ScenarioDto {

    public record ScenarioSummary(
            UUID id,
            String title,
            Theme theme,
            int totalMissions,
            int completedMissions) {}

    public record ScenarioMissionItem(
            UUID id,
            String title,
            List<String> techniques,
            int xpReward,
            DifficultyLevel difficulty,
            String status) {}

    public record ScenarioDetail(
            UUID id,
            String title,
            String description,
            Theme theme,
            List<ScenarioMissionItem> missions,
            Map<String, Integer> userProgress) {}

    public record CreateScenarioRequest(
            @NotBlank String title,
            @NotBlank String description,
            @NotNull Theme theme,
            @NotNull Boolean enabled) {}

    public record UpdateScenarioRequest(
            @NotBlank String title,
            @NotBlank String description,
            @NotNull Theme theme,
            @NotNull Boolean enabled) {}

    public record ScenarioResponse(
            UUID id,
            String title,
            String description,
            Theme theme,
            int totalMissions,
            boolean enabled) {}

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
            Theme theme,
            boolean enabled,
            int totalMissions,
            List<ScenarioMissionSummary> missions) {}

    public record ReorderMissionsRequest(
            @NotNull List<@NotNull UUID> missionIds) {}
}
