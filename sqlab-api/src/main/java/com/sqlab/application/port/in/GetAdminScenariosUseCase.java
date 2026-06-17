package com.sqlab.application.port.in;

import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Scenario;

import java.util.List;
import java.util.UUID;

public interface GetAdminScenariosUseCase {

    record ScenarioListResult(Scenario scenario, int totalMissions) {}

    record ScenarioDetailResult(Scenario scenario, List<Mission> missions) {}

    List<ScenarioListResult> listAll();

    ScenarioDetailResult findById(UUID id);
}
