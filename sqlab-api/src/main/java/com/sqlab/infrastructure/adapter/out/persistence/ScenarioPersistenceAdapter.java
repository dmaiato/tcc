package com.sqlab.infrastructure.adapter.out.persistence;

import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.domain.model.Scenario;
import com.sqlab.infrastructure.adapter.out.persistence.entity.ThemeJpaEntity;
import com.sqlab.infrastructure.adapter.out.persistence.mapper.ScenarioMapper;
import com.sqlab.infrastructure.adapter.out.persistence.repository.MissionJpaRepository;
import com.sqlab.infrastructure.adapter.out.persistence.repository.ScenarioJpaRepository;
import com.sqlab.infrastructure.adapter.out.persistence.repository.ThemeJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@Transactional(readOnly = true)
public class ScenarioPersistenceAdapter implements ScenarioRepository {

    private final ScenarioJpaRepository jpaRepository;
    private final ThemeJpaRepository themeJpaRepository;
    private final MissionJpaRepository missionJpaRepository;
    private final ScenarioMapper mapper;

    public ScenarioPersistenceAdapter(ScenarioJpaRepository jpaRepository,
                                      ThemeJpaRepository themeJpaRepository,
                                      MissionJpaRepository missionJpaRepository,
                                      ScenarioMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.themeJpaRepository = themeJpaRepository;
        this.missionJpaRepository = missionJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Scenario> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Scenario> findAllById(Set<UUID> ids) {
        return jpaRepository.findAllById(ids).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Scenario> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Scenario save(Scenario scenario) {
        ThemeJpaEntity themeEntity = themeJpaRepository.findByName(scenario.getTheme().name())
                .orElseThrow(() -> new IllegalArgumentException("Theme not found: " + scenario.getTheme()));
        return mapper.toDomain(jpaRepository.save(mapper.toJpa(scenario, themeEntity)));
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Scenario> findByEnabled() {
        return jpaRepository.findByEnabledTrue().stream().map(mapper::toDomain).toList();
    }

    @Override
    public int countMissionsByScenarioId(UUID scenarioId) {
        return missionJpaRepository.countByScenario_Id(scenarioId);
    }
}
