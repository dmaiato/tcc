package com.sqlab.infrastructure.adapter.out.persistence;

import com.sqlab.application.port.out.TechniqueRepository;
import com.sqlab.domain.model.Technique;
import com.sqlab.infrastructure.adapter.out.persistence.repository.TechniqueJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class TechniquePersistenceAdapter implements TechniqueRepository {

    private final TechniqueJpaRepository techniqueJpaRepository;

    public TechniquePersistenceAdapter(TechniqueJpaRepository techniqueJpaRepository) {
        this.techniqueJpaRepository = techniqueJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Technique> findByName(String name) {
        return techniqueJpaRepository.findByName(name)
                .map(t -> new Technique(t.getId(), t.getName()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Technique> findByNameIn(Set<String> names) {
        return techniqueJpaRepository.findByNameIn(names).stream()
                .map(t -> new Technique(t.getId(), t.getName()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Technique> findAll() {
        return techniqueJpaRepository.findAll().stream()
                .map(t -> new Technique(t.getId(), t.getName()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Technique> findById(UUID id) {
        return techniqueJpaRepository.findById(id)
                .map(t -> new Technique(t.getId(), t.getName()));
    }

    @Override
    @Transactional
    public Technique save(Technique technique) {
        var entity = techniqueJpaRepository.findByName(technique.getName())
                .orElse(new com.sqlab.infrastructure.adapter.out.persistence.entity.TechniqueJpaEntity());
        entity.setName(technique.getName());
        var saved = techniqueJpaRepository.save(entity);
        return new Technique(saved.getId(), saved.getName());
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        techniqueJpaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return techniqueJpaRepository.findByName(name).isPresent();
    }
}
