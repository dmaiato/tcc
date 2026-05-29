package com.sqlab.infrastructure.adapter.out.persistence.repository;

import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.domain.model.Theme;
import com.sqlab.infrastructure.adapter.out.persistence.entity.MissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface MissionJpaRepository extends JpaRepository<MissionJpaEntity, UUID>,
        JpaSpecificationExecutor<MissionJpaEntity> {

    List<MissionJpaEntity> findByTheme(Theme theme);
    List<MissionJpaEntity> findByDifficulty(DifficultyLevel difficulty);
    List<MissionJpaEntity> findByThemeAndDifficulty(Theme theme, DifficultyLevel difficulty);
    List<MissionJpaEntity> findByScenarioIdOrderByOrderIndex(UUID scenarioId);
    List<MissionJpaEntity> findByEnabledTrue();
    Optional<MissionJpaEntity> findByScenarioIdAndOrderIndex(UUID scenarioId, int orderIndex);
    int countByScenarioId(UUID scenarioId);
    int countByScenarioIdAndEnabledTrue(UUID scenarioId);
    boolean existsByScenarioIdAndEnabledFalse(UUID scenarioId);

    @Query("SELECT DISTINCT m.scenarioId FROM MissionJpaEntity m WHERE m.scenarioId IN :scenarioIds AND m.enabled = false")
    Set<UUID> findScenarioIdsWithDisabledMissions(@Param("scenarioIds") Set<UUID> scenarioIds);

    @Modifying
    @Query("UPDATE MissionJpaEntity m SET m.enabled = :enabled WHERE m.scenarioId = :scenarioId")
    int setEnabledByScenarioId(@Param("scenarioId") UUID scenarioId, @Param("enabled") boolean enabled);
}