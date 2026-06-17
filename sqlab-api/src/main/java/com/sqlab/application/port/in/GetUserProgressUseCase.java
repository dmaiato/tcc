package com.sqlab.application.port.in;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface GetUserProgressUseCase {

    record Query(UUID userId) {}

    record ProgressItem(UUID missionId, boolean completed, LocalDateTime completedAt,
                        String missionTitle, UUID scenarioId, String scenarioTitle) {}

    List<ProgressItem> handle(Query query);
}