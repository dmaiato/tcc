package com.sqlab.application.port.in;

import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.domain.model.Mission;

import java.util.List;
import java.util.UUID;

public interface GetMissionsUseCase {

    record ListAllQuery(String theme, DifficultyLevel difficulty) {
        public ListAllQuery() {
            this(null, null);
        }
    }

    record FindByIdQuery(UUID missionId, UUID userId) {
        public FindByIdQuery(UUID missionId) {
            this(missionId, null);
        }
    }

    List<Mission> handle(ListAllQuery query);

    Mission handle(FindByIdQuery query);

    record MissionDetail(Mission mission, Integer scenarioTotalMissions, String scenarioTitle) {}

    MissionDetail handleDetail(FindByIdQuery query);
}
