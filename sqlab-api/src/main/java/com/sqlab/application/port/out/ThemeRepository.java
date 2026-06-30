package com.sqlab.application.port.out;

import com.sqlab.domain.model.Theme;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ThemeRepository {
    Optional<Theme> findByName(String name);
    List<Theme> findAll();
    Optional<Theme> findById(UUID id);
    Theme save(Theme theme);
    void deleteById(UUID id);
    boolean existsByName(String name);
}
