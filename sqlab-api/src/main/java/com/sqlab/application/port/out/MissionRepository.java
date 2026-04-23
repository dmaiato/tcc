package com.sqlab.application.port.out;

import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Theme;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MissionRepository {
    Optional<Mission> findById(UUID id);
    List<Mission> findAll();
    List<Mission> findByTheme(Theme theme);
    List<Mission> findByDifficulty(DifficultyLevel difficulty);
    List<Mission> findByThemeAndDifficulty(Theme theme, DifficultyLevel difficulty);
}