package com.sqlab.application.port.out;

import com.sqlab.domain.model.Mission;

import java.util.UUID;

public interface MissionCommandPort {
    Mission save(Mission mission);
    void deleteById(UUID id);
    int setEnabledByScenarioId(UUID scenarioId, boolean enabled);
    void setOrderIndex(UUID id, int orderIndex);
}
