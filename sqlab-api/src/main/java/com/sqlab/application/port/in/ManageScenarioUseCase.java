package com.sqlab.application.port.in;

import com.sqlab.domain.model.Scenario;

import java.util.List;
import java.util.UUID;

public interface ManageScenarioUseCase {

    record CreateScenarioCommand(String title, String description, String themeName, Boolean enabled, int requiredLevel) {}
    record UpdateScenarioCommand(UUID id, String title, String description, String themeName, Boolean enabled, int requiredLevel) {}
    record ReorderMissionsCommand(UUID scenarioId, List<UUID> missionIds) {}

    Scenario create(CreateScenarioCommand command);

    Scenario update(UpdateScenarioCommand command);

    void delete(UUID scenarioId);

    void reorderMissions(ReorderMissionsCommand command);
    void setEnabled(UUID scenarioId, boolean enabled);

    int countMissionsByScenarioId(UUID scenarioId);
}
