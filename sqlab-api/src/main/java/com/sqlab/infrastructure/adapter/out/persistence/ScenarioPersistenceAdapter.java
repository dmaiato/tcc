package com.sqlab.infrastructure.adapter.out.persistence;

import com.sqlab.application.port.out.ScenarioMissionCascadePort;
import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.domain.model.Page;
import com.sqlab.domain.model.Scenario;
import com.sqlab.domain.model.Theme;
import com.sqlab.infrastructure.adapter.out.persistence.entity.ScenarioJpaEntity;
import com.sqlab.infrastructure.adapter.out.persistence.entity.ThemeJpaEntity;
import com.sqlab.infrastructure.adapter.out.persistence.mapper.ScenarioMapper;
import com.sqlab.infrastructure.adapter.out.persistence.repository.MissionJpaRepository;
import com.sqlab.infrastructure.adapter.out.persistence.repository.ScenarioJpaRepository;
import com.sqlab.infrastructure.adapter.out.persistence.repository.ThemeJpaRepository;
import com.sqlab.infrastructure.adapter.out.persistence.spec.ScenarioSpecifications;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@Transactional(readOnly = true)
public class ScenarioPersistenceAdapter implements ScenarioRepository, ScenarioMissionCascadePort {

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
        ThemeJpaEntity themeEntity = themeJpaRepository.findByName(scenario.getTheme().getName())
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

    @Override
    public Page<Scenario> findByFilters(String name, String themeName, int page, int size) {
        Specification<ScenarioJpaEntity> spec = ScenarioSpecifications.withFilters(name, themeName);
        Pageable pageable = PageRequest.of(page, size, Sort.by("title"));
        org.springframework.data.domain.Page<ScenarioJpaEntity> jpaPage = jpaRepository.findAll(spec, pageable);
        List<Scenario> domainContent = jpaPage.getContent().stream()
                .map(entity -> mapper.toDomain(entity))
                .toList();
        return new Page<>(
                domainContent,
                (int) jpaPage.getTotalElements(),
                jpaPage.getTotalPages(),
                jpaPage.getNumber(),
                jpaPage.getSize()
        );
    }

    @Override
    public Page<Scenario> findAllByFilters(String name, String themeName, Boolean enabled, int page, int size) {
        var spec = ScenarioSpecifications.withFiltersAdmin(name, themeName, enabled);
        var pageable = PageRequest.of(page, size, Sort.by("title"));
        org.springframework.data.domain.Page<ScenarioJpaEntity> jpaPage = jpaRepository.findAll(spec, pageable);
        List<Scenario> domainContent = jpaPage.getContent().stream()
                .map(mapper::toDomain)
                .toList();
        return new Page<>(
                domainContent,
                (int) jpaPage.getTotalElements(),
                jpaPage.getTotalPages(),
                jpaPage.getNumber(),
                jpaPage.getSize()
        );
    }

    @Override
    @Transactional
    public void setEnabled(UUID scenarioId, boolean enabled) {
        jpaRepository.findById(scenarioId).ifPresent(s -> {
            s.setEnabled(enabled);
            jpaRepository.save(s);
        });
        missionJpaRepository.setEnabledByScenarioId(scenarioId, enabled);
    }

    @Override
    public boolean existsByThemeName(String name) {
        return jpaRepository.existsByTheme_Name(name);
    }
}
