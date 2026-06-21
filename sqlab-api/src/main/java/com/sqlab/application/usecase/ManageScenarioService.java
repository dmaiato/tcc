package com.sqlab.application.usecase;

import com.sqlab.application.port.in.ManageScenarioUseCase;
import com.sqlab.application.port.out.MissionCommandPort;
import com.sqlab.application.port.out.MissionQueryPort;
import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.application.port.out.ThemeRepository;
import com.sqlab.domain.exception.ScenarioNotFoundException;
import com.sqlab.domain.exception.ThemeNotFoundException;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Scenario;
import com.sqlab.domain.model.Theme;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ManageScenarioService implements ManageScenarioUseCase {

    private final ScenarioRepository scenarioRepository;
    private final MissionQueryPort missionQueryPort;
    private final MissionCommandPort missionCommandPort;
    private final ThemeRepository themeRepository;

    public ManageScenarioService(ScenarioRepository scenarioRepository,
                                  MissionQueryPort missionQueryPort,
                                  MissionCommandPort missionCommandPort,
                                  ThemeRepository themeRepository) {
        this.scenarioRepository = scenarioRepository;
        this.missionQueryPort = missionQueryPort;
        this.missionCommandPort = missionCommandPort;
        this.themeRepository = themeRepository;
    }

    private Theme resolveTheme(String name) {
        return themeRepository.findByName(name.toUpperCase())
                .orElseThrow(() -> new ThemeNotFoundException(name));
    }

    @Override
    public Scenario create(CreateScenarioCommand command) {
        Theme theme = resolveTheme(command.themeName());
        Scenario scenario = new Scenario(
                UUID.randomUUID(),
                command.title(),
                command.description(),
                theme,
                command.enabled() != null ? command.enabled() : true,
                command.requiredLevel()
        );
        return scenarioRepository.save(scenario);
    }

    @Override
    public Scenario update(UpdateScenarioCommand command) {
        Scenario existing = scenarioRepository.findById(command.id())
                .orElseThrow(() -> new ScenarioNotFoundException(command.id()));

        Theme theme = resolveTheme(command.themeName());
        boolean enabled = command.enabled() != null ? command.enabled() : existing.isEnabled();
        boolean enabledChanged = enabled != existing.isEnabled();

        Scenario updated = new Scenario(
                command.id(),
                command.title(),
                command.description(),
                theme,
                enabled,
                command.requiredLevel()
        );

        if (enabledChanged) {
            missionCommandPort.setEnabledByScenarioId(command.id(), enabled);
        }

        return scenarioRepository.save(updated);
    }

    @Override
    public void setEnabled(UUID scenarioId, boolean enabled) {
        Scenario existing = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new ScenarioNotFoundException(scenarioId));

        if (enabled) {
            existing.enable();
        } else {
            existing.disable();
        }
        scenarioRepository.save(existing);
        missionCommandPort.setEnabledByScenarioId(scenarioId, enabled);
    }

    @Override
    public void delete(UUID scenarioId) {
        if (scenarioRepository.findById(scenarioId).isEmpty()) {
            throw new ScenarioNotFoundException(scenarioId);
        }
        scenarioRepository.deleteById(scenarioId);
    }

    @Override
    public void reorderMissions(ReorderMissionsCommand command) {
        if (scenarioRepository.findById(command.scenarioId()).isEmpty()) {
            throw new ScenarioNotFoundException(command.scenarioId());
        }

        List<Mission> existingMissions = missionQueryPort
                .findByScenarioIdOrderByOrderIndex(command.scenarioId());

        HashSet<UUID> existingIds = new HashSet<>();
        for (Mission m : existingMissions) {
            existingIds.add(m.getId());
        }

        if (command.missionIds().size() != existingIds.size()) {
            throw new IllegalArgumentException(
                    "Mission count mismatch: expected " + existingIds.size()
                            + " but got " + command.missionIds().size());
        }

        for (UUID mid : command.missionIds()) {
            if (!existingIds.contains(mid)) {
                throw new IllegalArgumentException("Mission " + mid
                        + " does not belong to scenario " + command.scenarioId());
            }
        }

        int size = command.missionIds().size();

        for (int i = 0; i < size; i++) {
            UUID missionId = command.missionIds().get(i);
            missionCommandPort.setOrderIndex(missionId, -(size + i + 1));
        }

        for (int i = 0; i < size; i++) {
            UUID missionId = command.missionIds().get(i);
            missionCommandPort.setOrderIndex(missionId, i + 1);
        }
    }

    @Override
    public int countMissionsByScenarioId(UUID scenarioId) {
        return missionQueryPort.countByScenarioId(scenarioId);
    }
}
