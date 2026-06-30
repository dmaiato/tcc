package com.sqlab.application.port.out;

import com.sqlab.domain.model.Technique;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TechniqueRepository {
    Optional<Technique> findByName(String name);

    List<Technique> findByNameIn(Set<String> names);

    List<Technique> findAll();

    Optional<Technique> findById(UUID id);

    Technique save(Technique technique);

    void deleteById(UUID id);

    boolean existsByName(String name);
}
