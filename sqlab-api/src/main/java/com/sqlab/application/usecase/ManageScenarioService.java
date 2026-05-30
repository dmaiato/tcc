package com.sqlab.application.usecase;

import com.sqlab.application.port.in.ManageScenarioUseCase;
import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.domain.exception.ScenarioNotFoundException;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Scenario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ManageScenarioService implements ManageScenarioUseCase {

    private final ScenarioRepository scenarioRepository;
    private final MissionRepository missionRepository;

    public ManageScenarioService(ScenarioRepository scenarioRepository,
                                  MissionRepository missionRepository) {
        this.scenarioRepository = scenarioRepository;
        this.missionRepository = missionRepository;
    }

    @Override
    public Scenario create(CreateScenarioCommand command) {
        Scenario scenario = new Scenario(
                UUID.randomUUID(),
                command.title(),
                command.description(),
                command.theme(),
                command.enabled() != null ? command.enabled() : true,
                command.requiredLevel()
        );
        return scenarioRepository.save(scenario);
    }

    @Override
    public Scenario update(UpdateScenarioCommand command) {
        Scenario existing = scenarioRepository.findById(command.id())
                .orElseThrow(() -> new ScenarioNotFoundException(command.id()));

        boolean enabled = command.enabled() != null ? command.enabled() : existing.isEnabled();
        boolean enabledChanged = enabled != existing.isEnabled();

        Scenario updated = new Scenario(
                command.id(),
                command.title(),
                command.description(),
                command.theme(),
                enabled,
                command.requiredLevel()
        );

        if (enabledChanged) {
            missionRepository.setEnabledByScenarioId(command.id(), enabled);
        }

        return scenarioRepository.save(updated);
    }

    @Override
    public void setEnabled(UUID scenarioId, boolean enabled) {
        Scenario existing = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new ScenarioNotFoundException(scenarioId));

        Scenario updated = new Scenario(
                existing.getId(),
                existing.getTitle(),
                existing.getDescription(),
                existing.getTheme(),
                enabled,
                existing.getRequiredLevel()
        );
        scenarioRepository.save(updated);
        missionRepository.setEnabledByScenarioId(scenarioId, enabled);
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

        List<Mission> existingMissions = missionRepository
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
            Mission original = existingMissions.stream()
                    .filter(m -> m.getId().equals(missionId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + missionId));

            Mission reordered = Mission.builder()
                    .id(original.getId())
                    .title(original.getTitle())
                    .briefing(original.getBriefing())
                    .objective(original.getObjective())
                    .hint(original.getHint())
                    .ddlScript(original.getDdlScript())
                    .dmlScript(original.getDmlScript())
                    .techniques(original.getTechniques())
                    .xpReward(original.getXpReward())
                    .expectedResult(original.getExpectedResult())
                    .ordered(original.isOrdered())
                    .theme(original.getTheme())
                    .difficulty(original.getDifficulty())
                    .scenarioId(original.getScenarioId())
                    .orderIndex(-(size + i + 1))
                    .scenarioTitle(original.getScenarioTitle())
                    .enabled(original.isEnabled())
                    .build();

            missionRepository.save(reordered);
        }

        for (int i = 0; i < size; i++) {
            UUID missionId = command.missionIds().get(i);
            Mission original = existingMissions.stream()
                    .filter(m -> m.getId().equals(missionId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + missionId));

            Mission reordered = Mission.builder()
                    .id(original.getId())
                    .title(original.getTitle())
                    .briefing(original.getBriefing())
                    .objective(original.getObjective())
                    .hint(original.getHint())
                    .ddlScript(original.getDdlScript())
                    .dmlScript(original.getDmlScript())
                    .techniques(original.getTechniques())
                    .xpReward(original.getXpReward())
                    .expectedResult(original.getExpectedResult())
                    .ordered(original.isOrdered())
                    .theme(original.getTheme())
                    .difficulty(original.getDifficulty())
                    .scenarioId(original.getScenarioId())
                    .orderIndex(i + 1)
                    .scenarioTitle(original.getScenarioTitle())
                    .enabled(original.isEnabled())
                    .build();

            missionRepository.save(reordered);
        }
    }
}
