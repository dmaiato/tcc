package com.sqlab.application.port.in;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ValidateMissionUseCase {

    record Command(UUID userId, UUID missionId, List<Map<String, Object>> submittedTuples) {}

    boolean handle(Command command);
}