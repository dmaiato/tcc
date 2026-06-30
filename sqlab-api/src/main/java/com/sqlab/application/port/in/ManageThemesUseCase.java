package com.sqlab.application.port.in;

import com.sqlab.domain.model.Theme;

import java.util.UUID;

public interface ManageThemesUseCase {

    record CreateThemeCommand(String name, String description, String emoji) {}

    record UpdateThemeCommand(UUID id, String name, String description, String emoji) {}

    Theme create(CreateThemeCommand command);

    Theme update(UpdateThemeCommand command);

    void delete(UUID id);
}
