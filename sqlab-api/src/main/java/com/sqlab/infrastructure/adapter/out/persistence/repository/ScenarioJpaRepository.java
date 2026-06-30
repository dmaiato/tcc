package com.sqlab.infrastructure.adapter.out.persistence.repository;

import com.sqlab.infrastructure.adapter.out.persistence.entity.ScenarioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ScenarioJpaRepository extends JpaRepository<ScenarioJpaEntity, UUID>, JpaSpecificationExecutor<ScenarioJpaEntity> {
    List<ScenarioJpaEntity> findByEnabledTrue();

    boolean existsByTheme_Name(String themeName);
}
