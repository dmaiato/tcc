package com.sqlab.application.port.in;

import com.sqlab.domain.model.ValidationResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ValidateMissionUseCase {

    record Command(UUID userId, UUID missionId, List<Map<String, Object>> submittedTuples) {}

    ValidationResult handle(Command command);
}