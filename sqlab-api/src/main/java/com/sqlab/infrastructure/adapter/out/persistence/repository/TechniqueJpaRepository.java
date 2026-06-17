package com.sqlab.infrastructure.adapter.out.persistence.repository;

import com.sqlab.infrastructure.adapter.out.persistence.entity.TechniqueJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TechniqueJpaRepository extends JpaRepository<TechniqueJpaEntity, UUID> {
    Optional<TechniqueJpaEntity> findByName(String name);

    List<TechniqueJpaEntity> findByNameIn(Set<String> names);
}
