package com.sqlab.infrastructure.adapter.out.persistence.repository;

import com.sqlab.infrastructure.adapter.out.persistence.entity.ProgressJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProgressJpaRepository extends JpaRepository<ProgressJpaEntity, UUID> {
    List<ProgressJpaEntity> findByUserId(UUID userId);
    List<ProgressJpaEntity> findByUserIdAndCompleted(UUID userId, boolean completed);
    boolean existsByUserIdAndMissionId(UUID userId, UUID missionId);
}