package com.sqlab.application.usecase;

import com.sqlab.application.port.in.ManageMissionUseCase;
import com.sqlab.application.port.out.MissionCommandPort;
import com.sqlab.application.port.out.MissionQueryPort;
import com.sqlab.application.port.out.ScenarioMissionCascadePort;
import com.sqlab.application.port.out.TechniqueRepository;
import com.sqlab.application.port.out.ThemeRepository;
import com.sqlab.domain.exception.MissionNotFoundException;
import com.sqlab.domain.exception.ThemeNotFoundException;
import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.domain.model.ExpectedTuple;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Technique;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ManageMissionService implements ManageMissionUseCase {

    private final MissionQueryPort missionQueryPort;
    private final MissionCommandPort missionCommandPort;
    private final ScenarioMissionCascadePort scenarioMissionCascadePort;
    private final ThemeRepository themeRepository;
    private final TechniqueRepository techniqueRepository;

    public ManageMissionService(MissionQueryPort missionQueryPort,
                                MissionCommandPort missionCommandPort,
                                ScenarioMissionCascadePort scenarioMissionCascadePort,
                                ThemeRepository themeRepository,
                                TechniqueRepository techniqueRepository) {
        this.missionQueryPort = missionQueryPort;
        this.missionCommandPort = missionCommandPort;
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

    private Set<Technique> resolveTechniques(Set<String> names) {
        if (names == null || names.isEmpty()) return Set.of();

        final Set<Technique> found = techniqueRepository.findByNameIn(names);

        if (found.size() < names.size()) {
            Set<String> foundNames = found.stream().map(Technique::name).collect(Collectors.toSet());

            names.removeIf(name -> foundNames.contains(name));

            throw new IllegalArgumentException("Techniques not found: " + names);
        }

        return found;
    }

    private Mission buildMission(UUID id, String title, String briefing,
                                  String objective, String hint,
                                  String ddlScript, String dmlScript,
                                  Set<Technique> techniques, int xpReward,
                                  List<Map<String, Object>> expectedResult,
                                  boolean ordered, com.sqlab.domain.model.Theme theme,
                                  DifficultyLevel difficulty,
                                  UUID scenarioId, Integer orderIndex,
                                  boolean enabled) {
        return Mission.builder()
                .id(id)
                .title(title)
                .briefing(briefing)
                .objective(objective)
                .hint(hint)
                .ddlScript(ddlScript)
                .dmlScript(dmlScript)
                .techniques(techniques)
                .xpReward(xpReward)
                .expectedResult(new ExpectedTuple(expectedResult))
                .ordered(ordered)
                .theme(theme)
                .difficulty(difficulty)
                .scenarioId(scenarioId)
                .orderIndex(orderIndex)
                .enabled(enabled)
                .build();
    }

    @Override
    public Mission create(CreateMissionCommand command) {
        UUID scenarioId = command.scenarioId();
        Integer orderIndex = command.orderIndex();
        if (scenarioId != null && orderIndex == null) {
            orderIndex = missionQueryPort.countByScenarioId(scenarioId) + 1;
        }
        var theme = resolveTheme(command.themeName());
        var techniques = resolveTechniques(command.techniqueNames());
        Mission mission = buildMission(
                UUID.randomUUID(),
                command.title(), command.briefing(), command.objective(),
                command.hint(), command.ddlScript(), command.dmlScript(),
                techniques, command.xpReward(), command.expectedResult(),
                command.ordered(), theme, command.difficulty(),
                scenarioId, orderIndex,
                command.enabled() != null ? command.enabled() : true);

        return missionCommandPort.save(mission);
    }

    @Override
    public Mission update(UpdateMissionCommand command) {
        validateScenarioConstraint(command.scenarioId(), command.orderIndex());
        Mission existing = missionQueryPort.findById(command.id())
                .orElseThrow(() -> new MissionNotFoundException(command.id()));

        var theme = resolveTheme(command.themeName());
        var techniques = resolveTechniques(command.techniqueNames());
        Mission updated = buildMission(
                command.id(),
                command.title(), command.briefing(), command.objective(),
                command.hint(), command.ddlScript(), command.dmlScript(),
                techniques, command.xpReward(), command.expectedResult(),
                command.ordered(), theme, command.difficulty(),
                command.scenarioId() != null ? command.scenarioId() : existing.getScenarioId(),
                command.orderIndex() != null ? command.orderIndex() : existing.getOrderIndex(),
                command.enabled() != null ? command.enabled() : existing.isEnabled());

        if (command.enabled() != null
                && command.enabled() != existing.isEnabled()
                && existing.getScenarioId() != null) {
            scenarioMissionCascadePort.setEnabled(existing.getScenarioId(), command.enabled());
        }

        return missionCommandPort.save(updated);
    }

    @Override
    public void delete(UUID missionId) {
        if (missionQueryPort.findById(missionId).isEmpty()) {
            throw new MissionNotFoundException(missionId);
        }
        missionCommandPort.deleteById(missionId);
    }

    @Override
    @Transactional(readOnly = true)
    public Mission findById(UUID missionId) {
        return missionQueryPort.findById(missionId)
                .orElseThrow(() -> new MissionNotFoundException(missionId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mission> findAll() {
        return missionQueryPort.findAll();
    }
}
