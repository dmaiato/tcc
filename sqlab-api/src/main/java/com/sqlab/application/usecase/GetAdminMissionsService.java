package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetAdminMissionsUseCase;
import com.sqlab.application.port.out.MissionQueryPort;
import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.domain.model.Mission;
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
        Mission mission = missionQueryPort.findById(id)
                .orElseThrow(() -> new com.sqlab.domain.exception.MissionNotFoundException(id));
        return toResult(mission);
    }

    private AdminMissionResult toResult(Mission mission) {
        String scenarioTitle = null;
        Integer scenarioTotalMissions = null;
        if (mission.getScenarioId() != null) {
            scenarioTitle = scenarioRepository.findById(mission.getScenarioId())
                    .map(com.sqlab.domain.model.Scenario::getTitle).orElse(null);
            scenarioTotalMissions = missionQueryPort.countByScenarioIdAndEnabledTrue(mission.getScenarioId());
        }
        return new AdminMissionResult(mission, scenarioTitle, scenarioTotalMissions);
    }
}
