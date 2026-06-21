package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetMissionsUseCase;
import com.sqlab.application.port.out.MissionQueryPort;
import com.sqlab.application.port.out.MissionValidationPort;
import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.application.port.out.ThemeRepository;
import com.sqlab.domain.exception.LevelRequiredException;
import com.sqlab.domain.exception.MissionLockedException;
import com.sqlab.domain.exception.MissionNotFoundException;
import com.sqlab.domain.exception.ThemeNotFoundException;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Theme;
import com.sqlab.domain.model.User;
import com.sqlab.domain.model.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetMissionsService implements GetMissionsUseCase {

    private final MissionQueryPort missionQueryPort;
    private final MissionValidationPort missionValidationPort;
    private final ScenarioRepository scenarioRepository;
    private final MissionAccessValidator missionAccessValidator;
    private final ThemeRepository themeRepository;

    public GetMissionsService(MissionQueryPort missionQueryPort,
                              MissionValidationPort missionValidationPort,
                              ScenarioRepository scenarioRepository,
                              MissionAccessValidator missionAccessValidator,
                              ThemeRepository themeRepository) {
        this.missionQueryPort = missionQueryPort;
        this.missionValidationPort = missionValidationPort;
        this.scenarioRepository = scenarioRepository;
        this.missionAccessValidator = missionAccessValidator;
        this.themeRepository = themeRepository;
    }

    private Theme resolveTheme(String name) {
        if (name == null) return null;
        return themeRepository.findByName(name.toUpperCase())
                .orElseThrow(() -> new ThemeNotFoundException(name));
    }

    @Override
    public List<Mission> handle(ListAllQuery query) {
        Theme theme = resolveTheme(query.theme());
        List<Mission> missions;
        if (theme != null && query.difficulty() != null) {
            missions = missionQueryPort.findByThemeAndDifficulty(theme, query.difficulty());
        } else if (theme != null) {
            missions = missionQueryPort.findByTheme(theme);
        } else if (query.difficulty() != null) {
            missions = missionQueryPort.findByDifficulty(query.difficulty());
        } else {
            missions = missionQueryPort.findByEnabledTrue();
        }

        List<Mission> enabledMissions = missions.stream()
                .filter(Mission::isEnabled)
                .toList();

        Set<UUID> scenarioIds = enabledMissions.stream()
                .map(Mission::getScenarioId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<UUID> disabledScenarios = missionValidationPort.findScenarioIdsWithDisabledMissions(scenarioIds);

        return enabledMissions.stream()
                .filter(m -> m.getScenarioId() == null || !disabledScenarios.contains(m.getScenarioId()))
                .toList();
    }

    @Override
    public Mission handle(FindByIdQuery query) {
        return missionAccessValidator.ensureAccessible(query.missionId(), query.userId());
    }

    @Override
    public MissionDetail handleDetail(FindByIdQuery query) {
        Mission mission = handle(query);
        Integer scenarioTotalMissions = null;
        String scenarioTitle = null;
        if (mission.getScenarioId() != null) {
            scenarioTotalMissions = missionQueryPort.countByScenarioIdAndEnabledTrue(mission.getScenarioId());
            scenarioTitle = scenarioRepository.findById(mission.getScenarioId())
                    .map(s -> s.getTitle())
                    .orElse(null);
        }
        return new MissionDetail(mission, scenarioTotalMissions, scenarioTitle);
    }
}
