package com.sqlab.infrastructure.adapter.out.persistence;

import com.sqlab.application.port.out.ThemeRepository;
import com.sqlab.domain.model.Theme;
import com.sqlab.infrastructure.adapter.out.persistence.repository.ThemeJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ThemePersistenceAdapter implements ThemeRepository {

    private final ThemeJpaRepository themeJpaRepository;

    public ThemePersistenceAdapter(ThemeJpaRepository themeJpaRepository) {
        this.themeJpaRepository = themeJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Theme> findByName(String name) {
        return themeJpaRepository.findByName(name)
                .map(t -> new Theme(t.getId(), t.getName(), t.getDescription(), t.getEmoji()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Theme> findAll() {
        return themeJpaRepository.findAll().stream()
                .map(t -> new Theme(t.getId(), t.getName(), t.getDescription(), t.getEmoji()))
                .sorted(Comparator.comparing(Theme::getName))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Theme> findById(UUID id) {
        return themeJpaRepository.findById(id)
                .map(t -> new Theme(t.getId(), t.getName(), t.getDescription(), t.getEmoji()));
    }

    @Override
    @Transactional
    public Theme save(Theme theme) {
        com.sqlab.infrastructure.adapter.out.persistence.entity.ThemeJpaEntity entity;
        if (theme.getId() != null) {
            entity = themeJpaRepository.getReferenceById(theme.getId());
        } else {
            entity = themeJpaRepository.findByName(theme.getName())
                    .orElse(new com.sqlab.infrastructure.adapter.out.persistence.entity.ThemeJpaEntity());
        }
        entity.setName(theme.getName());
        entity.setDescription(theme.getDescription());
        entity.setEmoji(theme.getEmoji());
        var saved = themeJpaRepository.save(entity);
        return new Theme(saved.getId(), saved.getName(), saved.getDescription(), saved.getEmoji());
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        themeJpaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return themeJpaRepository.findByName(name).isPresent();
    }
}
