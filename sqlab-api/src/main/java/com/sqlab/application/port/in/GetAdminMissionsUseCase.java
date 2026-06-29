package com.sqlab.application.port.in;

import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Page;

import java.util.List;
import java.util.UUID;

public interface GetAdminMissionsUseCase {

    record AdminMissionResult(Mission mission, String scenarioTitle, Integer scenarioTotalMissions) {}

    List<AdminMissionResult> listAll();

    Page<AdminMissionResult> listAll(String name, String theme, DifficultyLevel difficulty, String scenarioScope, Boolean enabled, int page, int size);

    AdminMissionResult findById(UUID id);
}
