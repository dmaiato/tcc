package com.sqlab.application.port.in;

import com.sqlab.domain.model.Mission;

import java.util.List;
import java.util.UUID;

public interface GetAdminMissionsUseCase {

    record AdminMissionResult(Mission mission, String scenarioTitle, Integer scenarioTotalMissions) {}

    List<AdminMissionResult> listAll();

    AdminMissionResult findById(UUID id);
}
