package com.sqlab.application.port.in;

import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Page;

import java.util.UUID;

public interface GetMissionsUseCase {

    record ListAllQuery(String theme, DifficultyLevel difficulty, String name, String scenarioScope, int page, int size) {
        public ListAllQuery() {
            this(null, null, null, null, 0, 12);
        }

        public ListAllQuery(String theme, DifficultyLevel difficulty) {
            this(theme, difficulty, null, null, 0, 12);
        }
    }

    record FindByIdQuery(UUID missionId, UUID userId) {
        public FindByIdQuery(UUID missionId) {
            this(missionId, null);
        }
    }

    Page<Mission> handle(ListAllQuery query);

    Mission handle(FindByIdQuery query);

    record MissionDetail(Mission mission, Integer scenarioTotalMissions, String scenarioTitle) {}

    MissionDetail handleDetail(FindByIdQuery query);
}
