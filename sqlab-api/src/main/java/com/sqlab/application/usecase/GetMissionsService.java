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
import com.sqlab.domain.model.Page;
import com.sqlab.domain.model.Theme;
import com.sqlab.domain.model.User;
import com.sqlab.domain.model.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
    public Page<Mission> handle(ListAllQuery query) {
        Theme theme = resolveTheme(query.theme());
        return missionQueryPort.findByFilters(
                query.name(), theme, query.difficulty(), query.scenarioScope(),
                query.page(), query.size()
        );
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
