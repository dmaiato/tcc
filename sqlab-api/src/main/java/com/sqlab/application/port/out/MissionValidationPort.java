package com.sqlab.application.port.out;

import java.util.Set;
import java.util.UUID;

public interface MissionValidationPort {
    boolean isPreviousMissionCompleted(UUID userId, UUID scenarioId, int orderIndex);
    boolean existsByScenarioIdAndEnabledFalse(UUID scenarioId);
    Set<UUID> findScenarioIdsWithDisabledMissions(Set<UUID> scenarioIds);
}
