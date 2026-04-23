package com.sqlab.infrastructure.adapter.in.web.dto;

import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.domain.model.Theme;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MissionDto {

    public record MissionResponse(
            UUID id,
            String title,
            String briefing,
            String ddlScript,
            String dmlScript,
            List<String> techniques,
            int xpReward,
            boolean ordered,
            Theme theme,
            DifficultyLevel difficulty) {}

    public record MissionSummary(
            UUID id,
            String title,
            List<String> techniques,
            int xpReward,
            boolean ordered,
            Theme theme,
            DifficultyLevel difficulty) {}

    public record ValidationRequest(@NotNull List<Map<String, Object>> tuples) {}

    public record ValidationResponse(boolean correct) {}
}