package com.sqlab.application.port.in;

import com.sqlab.domain.model.Theme;

import java.util.List;

public interface GetThemesUseCase {
    List<Theme> getAllThemes();
}
