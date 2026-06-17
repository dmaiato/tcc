package com.sqlab.application.port.out;

import com.sqlab.domain.model.Theme;

import java.util.Optional;

public interface ThemeRepository {
    Optional<Theme> findByName(String name);
}
