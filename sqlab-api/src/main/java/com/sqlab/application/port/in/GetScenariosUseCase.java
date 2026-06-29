package com.sqlab.application.port.in;

import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Page;
import com.sqlab.domain.model.Scenario;
import com.sqlab.domain.model.Theme;

import java.util.List;
import java.util.UUID;

public interface GetScenariosUseCase {
    record MissionStatus(Mission mission, String status) {}

    record ScenarioDetailResult(Scenario scenario, List<MissionStatus> missions,
                                int completedCount, int totalCount) {}

    record ScenarioSummaryResult(UUID id, String title, int totalMissions,
                                 int completedMissions, int requiredLevel, Theme theme) {}

    List<Scenario> handle();

    List<Scenario> handleEnabled();

    List<ScenarioSummaryResult> handleEnabledWithProgress(UUID userId);

    Page<ScenarioSummaryResult> handleEnabledWithProgress(UUID userId, String name, String themeName, int page, int size);

    Scenario handle(UUID id);

    ScenarioDetailResult handleDetail(UUID scenarioId, UUID userId);
}
