package com.sqlab.infrastructure.adapter.out.persistence.repository;

import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.infrastructure.adapter.out.persistence.entity.MissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public interface MissionJpaRepository extends JpaRepository<MissionJpaEntity, UUID> {

    Stream<MissionJpaEntity> findByTheme_Name(String themeName);
    Stream<MissionJpaEntity> findByDifficulty(DifficultyLevel difficulty);
    Stream<MissionJpaEntity> findByTheme_NameAndDifficulty(String themeName, DifficultyLevel difficulty);
    Stream<MissionJpaEntity> findByScenario_IdOrderByOrderIndex(UUID scenarioId);
    Stream<MissionJpaEntity> findByScenario_IdInOrderByOrderIndex(Set<UUID> scenarioIds);
    Stream<MissionJpaEntity> findByEnabledTrue();
    Optional<MissionJpaEntity> findByScenario_IdAndOrderIndex(UUID scenarioId, int orderIndex);
    int countByScenario_Id(UUID scenarioId);
    int countByScenario_IdAndEnabledTrue(UUID scenarioId);
    boolean existsByScenario_IdAndEnabledFalse(UUID scenarioId);

    @Query("SELECT DISTINCT m.scenario.id FROM MissionJpaEntity m WHERE m.scenario.id IN :scenarioIds AND m.enabled = false")
    Set<UUID> findScenarioIdsWithDisabledMissions(@Param("scenarioIds") Set<UUID> scenarioIds);

    @Modifying
    @Query("UPDATE MissionJpaEntity m SET m.enabled = :enabled WHERE m.scenario.id = :scenarioId")
    int setEnabledByScenarioId(@Param("scenarioId") UUID scenarioId, @Param("enabled") boolean enabled);

    @Modifying
    @Query("UPDATE MissionJpaEntity m SET m.orderIndex = :orderIndex WHERE m.id = :id")
    void setOrderIndex(@Param("id") UUID id, @Param("orderIndex") int orderIndex);
}
