package com.sqlab.infrastructure.adapter.out.persistence.repository;

import com.sqlab.infrastructure.adapter.out.persistence.entity.ThemeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ThemeJpaRepository extends JpaRepository<ThemeJpaEntity, UUID> {
    Optional<ThemeJpaEntity> findByName(String name);
}
