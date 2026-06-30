package com.sqlab.application.port.in;

import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.domain.model.Mission;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface ManageMissionUseCase {

    record CreateMissionCommand(
            String title,
            String briefing,
            String objective,
            String hint,
            String ddlScript,
            String dmlScript,
            Set<String> techniqueNames,
            int xpReward,
            boolean ordered,
            String themeName,
            DifficultyLevel difficulty,
            List<Map<String, Object>> expectedResult,
            UUID scenarioId,
            Integer orderIndex,
            Boolean enabled) {}

    record UpdateMissionCommand(
            UUID id,
            String title,
            String briefing,
            String objective,
            String hint,
            String ddlScript,
            String dmlScript,
            Set<String> techniqueNames,
            int xpReward,
            boolean ordered,
            String themeName,
            DifficultyLevel difficulty,
            List<Map<String, Object>> expectedResult,
            UUID scenarioId,
            Integer orderIndex,
            Boolean enabled) {}

    Mission create(CreateMissionCommand command);

    Mission update(UpdateMissionCommand command);

    void delete(UUID missionId);

    Mission findById(UUID missionId);

    List<Mission> findAll();
}
