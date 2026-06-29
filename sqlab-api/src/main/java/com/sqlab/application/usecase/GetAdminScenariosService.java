package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetAdminScenariosUseCase;
import com.sqlab.application.port.out.MissionQueryPort;
import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.domain.model.Page;
import com.sqlab.domain.model.Scenario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetAdminScenariosService implements GetAdminScenariosUseCase {

    private final ScenarioRepository scenarioRepository;
    private final MissionQueryPort missionQueryPort;

    public GetAdminScenariosService(ScenarioRepository scenarioRepository,
                                    MissionQueryPort missionQueryPort) {
        this.scenarioRepository = scenarioRepository;
        this.missionQueryPort = missionQueryPort;
    }

    @Override
    public List<ScenarioListResult> listAll() {
        return scenarioRepository.findAll().stream()
                .map(s -> new ScenarioListResult(s, missionQueryPort.countByScenarioId(s.getId())))
                .toList();
    }

    @Override
    public Page<ScenarioListResult> listAll(String name, String theme, Boolean enabled, int page, int size) {
        Page<Scenario> scenarioPage = scenarioRepository.findAllByFilters(name, theme, enabled, page, size);
        List<ScenarioListResult> results = scenarioPage.content().stream()
                .map(s -> new ScenarioListResult(s, missionQueryPort.countByScenarioId(s.getId())))
                .toList();
        return new Page<>(results, scenarioPage.totalElements(), scenarioPage.totalPages(), scenarioPage.number(), scenarioPage.size());
    }

    @Override
    public ScenarioDetailResult findById(UUID id) {
        Scenario scenario = scenarioRepository.findById(id)
                .orElseThrow(() -> new com.sqlab.domain.exception.ScenarioNotFoundException(id));
        var missions = missionQueryPort.findByScenarioIdOrderByOrderIndex(id);
        return new ScenarioDetailResult(scenario, missions);
    }
}
