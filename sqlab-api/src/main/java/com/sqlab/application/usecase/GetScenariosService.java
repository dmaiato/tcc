package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetScenariosUseCase;
import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.domain.exception.ScenarioNotFoundException;
import com.sqlab.domain.model.Scenario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetScenariosService implements GetScenariosUseCase {

    private final ScenarioRepository scenarioRepository;

    public GetScenariosService(ScenarioRepository scenarioRepository) {
        this.scenarioRepository = scenarioRepository;
    }

    @Override
    public List<Scenario> handle() {
        return scenarioRepository.findAll();
    }

    @Override
    public List<Scenario> handleEnabled() {
        return scenarioRepository.findByEnabled();
    }

    @Override
    public Scenario handle(UUID id) {
        return scenarioRepository.findById(id)
                .orElseThrow(() -> new ScenarioNotFoundException(id));
    }
}
