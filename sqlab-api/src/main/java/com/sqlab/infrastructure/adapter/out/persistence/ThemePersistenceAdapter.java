package com.sqlab.infrastructure.adapter.out.persistence;

import com.sqlab.application.port.out.ThemeRepository;
import com.sqlab.domain.model.Theme;
import com.sqlab.infrastructure.adapter.out.persistence.repository.ThemeJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class ThemePersistenceAdapter implements ThemeRepository {

    private final ThemeJpaRepository themeJpaRepository;

    public ThemePersistenceAdapter(ThemeJpaRepository themeJpaRepository) {
        this.themeJpaRepository = themeJpaRepository;
    }

    @Override
    public Optional<Theme> findByName(String name) {
        return themeJpaRepository.findByName(name)
                .map(t -> new Theme(t.getId(), t.getName(), t.getDescription(), t.getEmoji()));
    }
}
