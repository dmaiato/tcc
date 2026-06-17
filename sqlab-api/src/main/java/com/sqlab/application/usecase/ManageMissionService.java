package com.sqlab.application.usecase;

import com.sqlab.application.port.in.ManageMissionUseCase;
import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.application.port.out.ScenarioMissionCascadePort;
import com.sqlab.application.port.out.TechniqueRepository;
import com.sqlab.application.port.out.ThemeRepository;
import com.sqlab.domain.exception.MissionNotFoundException;
import com.sqlab.domain.exception.ThemeNotFoundException;
import com.sqlab.domain.model.ExpectedTuple;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Technique;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ManageMissionService implements ManageMissionUseCase {

    private final MissionRepository missionRepository;
    private final ScenarioMissionCascadePort scenarioMissionCascadePort;
    private final ThemeRepository themeRepository;
    private final TechniqueRepository techniqueRepository;

    public ManageMissionService(MissionRepository missionRepository,
                                ScenarioMissionCascadePort scenarioMissionCascadePort,
                                ThemeRepository themeRepository,
                                TechniqueRepository techniqueRepository) {
        this.missionRepository = missionRepository;
        this.scenarioMissionCascadePort = scenarioMissionCascadePort;
        this.themeRepository = themeRepository;
        this.techniqueRepository = techniqueRepository;
    }

    private void validateScenarioConstraint(UUID scenarioId, Integer orderIndex) {
        if (scenarioId != null && orderIndex == null) {
            throw new IllegalArgumentException("orderIndex is required when scenarioId is provided");
        }
    }

    private com.sqlab.domain.model.Theme resolveTheme(String name) {
        return themeRepository.findByName(name.toUpperCase())
                .orElseThrow(() -> new ThemeNotFoundException(name));
    }

    private List<Technique> resolveTechniques(List<String> names) {
        if (names == null || names.isEmpty()) return List.of();
        Set<String> uniqueNames = Set.copyOf(names);
        List<Technique> found = techniqueRepository.findByNameIn(uniqueNames);
        if (found.size() != uniqueNames.size()) {
            Set<String> foundNames = found.stream().map(Technique::getName).collect(Collectors.toSet());
            var missing = new java.util.HashSet<>(uniqueNames);
            missing.removeAll(foundNames);
            throw new IllegalArgumentException("Techniques not found: " + missing);
        }
        Map<String, Technique> techniqueByName = found.stream()
                .collect(Collectors.toMap(Technique::getName, t -> t));
        return names.stream().map(techniqueByName::get).toList();
    }

    @Override
    public Mission create(CreateMissionCommand command) {
        UUID scenarioId = command.scenarioId();
        Integer orderIndex = command.orderIndex();
        if (scenarioId != null && orderIndex == null) {
            orderIndex = missionRepository.countByScenarioId(scenarioId) + 1;
        }
        var theme = resolveTheme(command.themeName());
        var techniques = resolveTechniques(command.techniqueNames());
        Mission mission = Mission.builder()
                .id(UUID.randomUUID())
                .title(command.title())
                .briefing(command.briefing())
                .objective(command.objective())
                .hint(command.hint())
                .ddlScript(command.ddlScript())
                .dmlScript(command.dmlScript())
                .techniques(techniques)
                .xpReward(command.xpReward())
                .expectedResult(new ExpectedTuple(command.expectedResult()))
                .ordered(command.ordered())
                .theme(theme)
                .difficulty(command.difficulty())
                .scenarioId(scenarioId)
                .orderIndex(orderIndex)
                .enabled(command.enabled() != null ? command.enabled() : true)
                .build();

        return missionRepository.save(mission);
    }

    @Override
    public Mission update(UpdateMissionCommand command) {
        validateScenarioConstraint(command.scenarioId(), command.orderIndex());
        Mission existing = missionRepository.findById(command.id())
                .orElseThrow(() -> new MissionNotFoundException(command.id()));

        var theme = resolveTheme(command.themeName());
        var techniques = resolveTechniques(command.techniqueNames());

        Mission updated = Mission.builder()
                .id(command.id())
                .title(command.title())
                .briefing(command.briefing())
                .objective(command.objective())
                .hint(command.hint())
                .ddlScript(command.ddlScript())
                .dmlScript(command.dmlScript())
                .techniques(techniques)
                .xpReward(command.xpReward())
                .expectedResult(new ExpectedTuple(command.expectedResult()))
                .ordered(command.ordered())
                .theme(theme)
                .difficulty(command.difficulty())
                .scenarioId(command.scenarioId() != null ? command.scenarioId() : existing.getScenarioId())
                .orderIndex(command.orderIndex() != null ? command.orderIndex() : existing.getOrderIndex())
                .enabled(command.enabled() != null ? command.enabled() : existing.isEnabled())
                .build();

        if (command.enabled() != null
                && command.enabled() != existing.isEnabled()
                && existing.getScenarioId() != null) {
            scenarioMissionCascadePort.setEnabled(existing.getScenarioId(), command.enabled());
        }

        return missionRepository.save(updated);
    }

    @Override
    public void delete(UUID missionId) {
        if (missionRepository.findById(missionId).isEmpty()) {
            throw new MissionNotFoundException(missionId);
        }
        missionRepository.deleteById(missionId);
    }

    @Override
    @Transactional(readOnly = true)
    public Mission findById(UUID missionId) {
        return missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionNotFoundException(missionId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mission> findAll() {
        return missionRepository.findAll();
    }
}
