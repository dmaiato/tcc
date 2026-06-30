package com.sqlab.application.usecase;

import com.sqlab.application.port.in.ManageThemesUseCase;
import com.sqlab.application.port.out.MissionQueryPort;
import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.application.port.out.ThemeRepository;
import com.sqlab.domain.exception.ThemeNotFoundException;
import com.sqlab.domain.model.Theme;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ManageThemesService implements ManageThemesUseCase {

    private final ThemeRepository themeRepository;
    private final MissionQueryPort missionQueryPort;
    private final ScenarioRepository scenarioRepository;

    public ManageThemesService(ThemeRepository themeRepository,
                               MissionQueryPort missionQueryPort,
                               ScenarioRepository scenarioRepository) {
        this.themeRepository = themeRepository;
        this.missionQueryPort = missionQueryPort;
        this.scenarioRepository = scenarioRepository;
    }

    @Override
    @Transactional
    public Theme create(CreateThemeCommand command) {
        if (command.name() == null || command.name().isBlank()) {
            throw new IllegalArgumentException("Theme name must not be blank");
        }
        String normalized = command.name().trim().toUpperCase();
        if (themeRepository.existsByName(normalized)) {
            throw new IllegalArgumentException("Theme already exists: " + normalized);
        }
        var theme = new Theme(null, normalized, command.description(), command.emoji());
        return themeRepository.save(theme);
    }

    @Override
    @Transactional
    public Theme update(UpdateThemeCommand command) {
        var existing = themeRepository.findById(command.id())
                .orElseThrow(() -> new ThemeNotFoundException(command.id().toString()));
        if (command.name() == null || command.name().isBlank()) {
            throw new IllegalArgumentException("Theme name must not be blank");
        }
        String normalized = command.name().trim().toUpperCase();
        if (!normalized.equals(existing.getName()) && themeRepository.existsByName(normalized)) {
            throw new IllegalArgumentException("Theme already exists: " + normalized);
        }
        var updated = new Theme(existing.getId(), normalized, command.description(), command.emoji());
        return themeRepository.save(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        var theme = themeRepository.findById(id)
                .orElseThrow(() -> new ThemeNotFoundException(id.toString()));
        if (missionQueryPort.existsByThemeName(theme.getName())) {
            throw new IllegalStateException("Cannot delete theme '" + theme.getName()
                    + "' because it is referenced by one or more missions");
        }
        if (scenarioRepository.existsByThemeName(theme.getName())) {
            throw new IllegalStateException("Cannot delete theme '" + theme.getName()
                    + "' because it is referenced by one or more scenarios");
        }
        themeRepository.deleteById(id);
    }
}
