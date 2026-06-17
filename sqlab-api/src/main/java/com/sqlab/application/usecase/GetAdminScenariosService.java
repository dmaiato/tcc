package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetAdminScenariosUseCase;
import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.domain.model.Scenario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetAdminScenariosService implements GetAdminScenariosUseCase {

    private final ScenarioRepository scenarioRepository;
    private final MissionRepository missionRepository;

    public GetAdminScenariosService(ScenarioRepository scenarioRepository,
                                    MissionRepository missionRepository) {
        this.scenarioRepository = scenarioRepository;
        this.missionRepository = missionRepository;
    }

    @Override
    public List<ScenarioListResult> listAll() {
        return scenarioRepository.findAll().stream()
                .map(s -> new ScenarioListResult(s, missionRepository.countByScenarioId(s.getId())))
                .toList();
    }

    @Override
    public ScenarioDetailResult findById(UUID id) {
        Scenario scenario = scenarioRepository.findById(id)
                .orElseThrow(() -> new com.sqlab.domain.exception.ScenarioNotFoundException(id));
        var missions = missionRepository.findByScenarioIdOrderByOrderIndex(id);
        return new ScenarioDetailResult(scenario, missions);
    }
}
