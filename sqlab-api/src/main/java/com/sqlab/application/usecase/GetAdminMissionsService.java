package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetAdminMissionsUseCase;
import com.sqlab.application.port.out.MissionQueryPort;
import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.domain.exception.MissionNotFoundException;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Scenario;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetAdminMissionsService implements GetAdminMissionsUseCase {

    private final MissionQueryPort missionQueryPort;
    private final ScenarioRepository scenarioRepository;

    public GetAdminMissionsService(MissionQueryPort missionQueryPort,
                                   ScenarioRepository scenarioRepository) {
        this.missionQueryPort = missionQueryPort;
        this.scenarioRepository = scenarioRepository;
    }

    @Override
    public List<AdminMissionResult> listAll() {
        return missionQueryPort.findAll().stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public AdminMissionResult findById(UUID id) {
        return missionQueryPort.findById(id)
            .map(this::toResult)
            .orElseThrow(() -> new MissionNotFoundException(id));
    }

    private AdminMissionResult toResult(Mission mission) {
        if (mission.hasScenario()) {
            final String scenarioTitle = scenarioRepository.findById(mission.getScenarioId())
                    .map(Scenario::getTitle).orElse(null);
            final Integer scenarioTotalMissions = missionQueryPort.countByScenarioIdAndEnabledTrue(mission.getScenarioId());
            return new AdminMissionResult(mission, scenarioTitle, scenarioTotalMissions);
        }
        return new AdminMissionResult(mission);
    }
}
