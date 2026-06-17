package com.sqlab.application.port.out;

import com.sqlab.domain.model.Technique;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TechniqueRepository {
    Optional<Technique> findByName(String name);

    List<Technique> findByNameIn(Set<String> names);
}
