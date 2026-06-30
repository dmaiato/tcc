package com.sqlab.application.port.out;

import com.sqlab.domain.model.Page;
import com.sqlab.domain.model.Scenario;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ScenarioRepository {
    List<Scenario> findAll();
    List<Scenario> findAllById(Set<UUID> ids);
    Optional<Scenario> findById(UUID id);
    Scenario save(Scenario scenario);
    void deleteById(UUID id);
    List<Scenario> findByEnabled();
    Page<Scenario> findByFilters(String name, String themeName, int page, int size);
    Page<Scenario> findAllByFilters(String name, String themeName, Boolean enabled, int page, int size);
    int countMissionsByScenarioId(UUID scenarioId);
    boolean existsByThemeName(String name);
}
