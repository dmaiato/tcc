package com.sqlab.application.port.in;

import com.sqlab.domain.model.Mission;

import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public interface GetAdminMissionsUseCase {

    record AdminMissionResult(@NonNull Mission mission,
                              @Nullable String scenarioTitle,
                              @Nullable Integer scenarioTotalMissions) {
        
        public AdminMissionResult(Mission mission) {
            this(mission, null, null);
        }
    }

    List<AdminMissionResult> listAll();

    AdminMissionResult findById(UUID id);
}
