package com.sqlab.application.port.out;

import com.sqlab.domain.model.Progress;

import java.util.List;
import java.util.UUID;

public interface ProgressRepository {
    Progress save(Progress progress);
    List<Progress> findByUserId(UUID userId);
    List<Progress> findCompletedByUserId(UUID userId);
    boolean existsByUserIdAndMissionId(UUID userId, UUID missionId);
}