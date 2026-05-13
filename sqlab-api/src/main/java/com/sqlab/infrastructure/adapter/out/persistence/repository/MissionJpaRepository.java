package com.sqlab.infrastructure.adapter.out.persistence.repository;

import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.domain.model.Theme;
import com.sqlab.infrastructure.adapter.out.persistence.entity.MissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MissionJpaRepository extends JpaRepository<MissionJpaEntity, UUID>,
        JpaSpecificationExecutor<MissionJpaEntity> {

    List<MissionJpaEntity> findByTheme(Theme theme);
    List<MissionJpaEntity> findByDifficulty(DifficultyLevel difficulty);
    List<MissionJpaEntity> findByThemeAndDifficulty(Theme theme, DifficultyLevel difficulty);
    List<MissionJpaEntity> findByScenarioIdOrderByOrderIndex(UUID scenarioId);
    Optional<MissionJpaEntity> findByScenarioIdAndOrderIndex(UUID scenarioId, int orderIndex);
    int countByScenarioId(UUID scenarioId);
}