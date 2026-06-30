package com.sqlab.infrastructure.adapter.out.persistence;

import com.sqlab.application.port.out.TechniqueRepository;
import com.sqlab.domain.model.Technique;
import com.sqlab.infrastructure.adapter.out.persistence.repository.TechniqueJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static java.util.stream.Collectors.toSet;

import java.util.Optional;
import java.util.Set;

@Component
@Transactional(readOnly = true)
public class TechniquePersistenceAdapter implements TechniqueRepository {

    private final TechniqueJpaRepository techniqueJpaRepository;

    public TechniquePersistenceAdapter(TechniqueJpaRepository techniqueJpaRepository) {
        this.techniqueJpaRepository = techniqueJpaRepository;
    }

    @Override
    public Optional<Technique> findByName(String name) {
        return techniqueJpaRepository.findByName(name)
                .map(t -> new Technique(t.getId(), t.getName()));
    }

    @Override
    public Set<Technique> findByNameIn(Set<String> names) {
        return techniqueJpaRepository.findByNameIn(names)
                .map(t -> new Technique(t.getId(), t.getName()))
                .collect(toSet());
    }
}
