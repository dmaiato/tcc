package com.sqlab.application.port.out;

import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Theme;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface MissionRepository {
    Optional<Mission> findById(UUID id);
    List<Mission> findAllById(Set<UUID> ids);
    List<Mission> findAll();
    List<Mission> findByTheme(Theme theme);
    List<Mission> findByDifficulty(DifficultyLevel difficulty);
    List<Mission> findByThemeAndDifficulty(Theme theme, DifficultyLevel difficulty);
    List<Mission> findByScenarioIdOrderByOrderIndex(UUID scenarioId);
    List<Mission> findByScenarioIdInOrderByOrderIndex(Set<UUID> scenarioIds);
    List<Mission> findByEnabledTrue();
    boolean existsByScenarioIdAndEnabledFalse(UUID scenarioId);
    Set<UUID> findScenarioIdsWithDisabledMissions(Set<UUID> scenarioIds);
    boolean isPreviousMissionCompleted(UUID userId, UUID scenarioId, int orderIndex);
    int countByScenarioId(UUID scenarioId);
    int countByScenarioIdAndEnabledTrue(UUID scenarioId);
    Mission save(Mission mission);
    int setEnabledByScenarioId(UUID scenarioId, boolean enabled);
    void setOrderIndex(UUID id, int orderIndex);
    void deleteById(UUID id);
}