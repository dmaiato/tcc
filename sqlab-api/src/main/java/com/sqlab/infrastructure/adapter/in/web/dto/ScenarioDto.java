package com.sqlab.infrastructure.adapter.in.web.dto;

import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.domain.model.Theme;

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
}
