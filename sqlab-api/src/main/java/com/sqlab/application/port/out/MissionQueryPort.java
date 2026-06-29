package com.sqlab.application.port.out;

import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Page;
import com.sqlab.domain.model.Theme;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface MissionQueryPort {
    Optional<Mission> findById(UUID id);
    List<Mission> findAll();
    List<Mission> findAllById(Set<UUID> ids);
    List<Mission> findByTheme(Theme theme);
    List<Mission> findByDifficulty(DifficultyLevel difficulty);
    List<Mission> findByThemeAndDifficulty(Theme theme, DifficultyLevel difficulty);
    List<Mission> findByScenarioIdOrderByOrderIndex(UUID scenarioId);
    List<Mission> findByScenarioIdInOrderByOrderIndex(Set<UUID> scenarioIds);
    List<Mission> findByEnabledTrue();
    Page<Mission> findByFilters(String name, Theme theme, DifficultyLevel difficulty, String scenarioScope, int page, int size);
    Page<Mission> findAllByFilters(String name, Theme theme, DifficultyLevel difficulty, String scenarioScope, Boolean enabled, int page, int size);
    int countByScenarioId(UUID scenarioId);
    int countByScenarioIdAndEnabledTrue(UUID scenarioId);
}
