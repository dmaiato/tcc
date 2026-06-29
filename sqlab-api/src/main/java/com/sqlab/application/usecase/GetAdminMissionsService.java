package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetAdminMissionsUseCase;
import com.sqlab.application.port.out.MissionQueryPort;
import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.application.port.out.ThemeRepository;
import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Page;
import com.sqlab.domain.model.Theme;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetAdminMissionsService implements GetAdminMissionsUseCase {

    private final MissionQueryPort missionQueryPort;
    private final ScenarioRepository scenarioRepository;
    private final ThemeRepository themeRepository;

    public GetAdminMissionsService(MissionQueryPort missionQueryPort,
                                   ScenarioRepository scenarioRepository,
                                   ThemeRepository themeRepository) {
        this.missionQueryPort = missionQueryPort;
        this.scenarioRepository = scenarioRepository;
        this.themeRepository = themeRepository;
    }

    @Override
    public List<AdminMissionResult> listAll() {
        return missionQueryPort.findAll().stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public AdminMissionResult findById(UUID id) {
        Mission mission = missionQueryPort.findById(id)
                .orElseThrow(() -> new com.sqlab.domain.exception.MissionNotFoundException(id));
        return toResult(mission);
    }

    @Override
    public Page<AdminMissionResult> listAll(String name, String theme, DifficultyLevel difficulty, String scenarioScope, Boolean enabled, int page, int size) {
        Theme themeObj = resolveTheme(theme);
        Page<Mission> missionPage = missionQueryPort.findAllByFilters(name, themeObj, difficulty, scenarioScope, enabled, page, size);
        List<AdminMissionResult> results = missionPage.content().stream()
                .map(this::toResult)
                .toList();
        return new Page<>(results, missionPage.totalElements(), missionPage.totalPages(), missionPage.number(), missionPage.size());
    }

    private Theme resolveTheme(String name) {
        if (name == null) return null;
        return themeRepository.findByName(name.toUpperCase())
                .orElseThrow(() -> new com.sqlab.domain.exception.ThemeNotFoundException(name));
    }

    private AdminMissionResult toResult(Mission mission) {
        String scenarioTitle = null;
        Integer scenarioTotalMissions = null;
        if (mission.getScenarioId() != null) {
            scenarioTitle = scenarioRepository.findById(mission.getScenarioId())
                    .map(s -> s.getTitle()).orElse(null);
            scenarioTotalMissions = missionQueryPort.countByScenarioId(mission.getScenarioId());
        }
        return new AdminMissionResult(mission, scenarioTitle, scenarioTotalMissions);
    }
}
