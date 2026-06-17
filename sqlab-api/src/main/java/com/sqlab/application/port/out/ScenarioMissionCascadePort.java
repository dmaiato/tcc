package com.sqlab.application.port.out;

import java.util.UUID;

public interface ScenarioMissionCascadePort {
    void setEnabled(UUID scenarioId, boolean enabled);
}
