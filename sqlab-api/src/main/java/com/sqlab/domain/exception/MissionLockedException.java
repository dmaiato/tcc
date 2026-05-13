package com.sqlab.domain.exception;

import java.util.UUID;

public class MissionLockedException extends RuntimeException {
    private final UUID missionId;
    private final UUID scenarioId;
    private final String scenarioTitle;

    public MissionLockedException(UUID missionId, UUID scenarioId, String scenarioTitle) {
        super("Mission locked: complete the previous mission in '" + scenarioTitle + "' first");
        this.missionId = missionId;
        this.scenarioId = scenarioId;
        this.scenarioTitle = scenarioTitle;
    }

    public UUID getMissionId() { return missionId; }
    public UUID getScenarioId() { return scenarioId; }
    public String getScenarioTitle() { return scenarioTitle; }
}
