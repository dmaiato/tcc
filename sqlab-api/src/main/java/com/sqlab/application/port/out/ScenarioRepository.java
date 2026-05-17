package com.sqlab.application.port.out;

import com.sqlab.domain.model.Scenario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScenarioRepository {
    List<Scenario> findAll();
    Optional<Scenario> findById(UUID id);
    Scenario save(Scenario scenario);
    void deleteById(UUID id);
    int countMissionsByScenarioId(UUID scenarioId);
}
