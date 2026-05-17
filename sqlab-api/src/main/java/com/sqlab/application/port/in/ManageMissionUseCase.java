package com.sqlab.application.port.in;

import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Theme;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ManageMissionUseCase {

    record CreateMissionCommand(
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
            List<Map<String, Object>> expectedResult,
            UUID scenarioId,
            Integer orderIndex) {}

    record UpdateMissionCommand(
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
            List<Map<String, Object>> expectedResult,
            UUID scenarioId,
            Integer orderIndex) {}

    Mission create(CreateMissionCommand command);

    Mission update(UpdateMissionCommand command);

    void delete(UUID missionId);

    Mission findById(UUID missionId);
}
