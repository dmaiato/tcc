package com.sqlab.infrastructure.adapter.out.persistence;

import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Technique;
import com.sqlab.domain.model.Theme;
import com.sqlab.infrastructure.adapter.out.persistence.entity.MissionJpaEntity;
import com.sqlab.infrastructure.adapter.out.persistence.entity.ScenarioJpaEntity;
import com.sqlab.infrastructure.adapter.out.persistence.entity.TechniqueJpaEntity;
import com.sqlab.infrastructure.adapter.out.persistence.entity.ThemeJpaEntity;
import com.sqlab.infrastructure.adapter.out.persistence.mapper.MissionMapper;
import com.sqlab.infrastructure.adapter.out.persistence.repository.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
public class MissionPersistenceAdapter implements MissionRepository {

    private final MissionJpaRepository jpaRepository;
    private final ThemeJpaRepository themeJpaRepository;
    private final TechniqueJpaRepository techniqueJpaRepository;
    private final ScenarioJpaRepository scenarioJpaRepository;
    private final MissionMapper mapper;
    private final ProgressJpaRepository progressJpaRepository;

    public MissionPersistenceAdapter(MissionJpaRepository jpaRepository,
                                     ThemeJpaRepository themeJpaRepository,
                                     TechniqueJpaRepository techniqueJpaRepository,
                                     ScenarioJpaRepository scenarioJpaRepository,
                                     MissionMapper mapper,
                                     ProgressJpaRepository progressJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.themeJpaRepository = themeJpaRepository;
        this.techniqueJpaRepository = techniqueJpaRepository;
        this.scenarioJpaRepository = scenarioJpaRepository;
        this.mapper = mapper;
        this.progressJpaRepository = progressJpaRepository;
    }

    @Override
    public Optional<Mission> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Mission> findAllById(Set<UUID> ids) {
        return jpaRepository.findAllById(ids).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Mission> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Mission> findByTheme(Theme theme) {
        return jpaRepository.findByTheme_Name(theme.getName()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Mission> findByDifficulty(DifficultyLevel difficulty) {
        return jpaRepository.findByDifficulty(difficulty).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Mission> findByThemeAndDifficulty(Theme theme, DifficultyLevel difficulty) {
        return jpaRepository.findByTheme_NameAndDifficulty(theme.getName(), difficulty).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Mission> findByScenarioIdOrderByOrderIndex(UUID scenarioId) {
        return jpaRepository.findByScenario_IdOrderByOrderIndex(scenarioId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Mission> findByScenarioIdInOrderByOrderIndex(Set<UUID> scenarioIds) {
        if (scenarioIds.isEmpty()) return List.of();
        return jpaRepository.findByScenario_IdInOrderByOrderIndex(scenarioIds)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Mission> findByEnabledTrue() {
        return jpaRepository.findByEnabledTrue().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByScenarioIdAndEnabledFalse(UUID scenarioId) {
        return jpaRepository.existsByScenario_IdAndEnabledFalse(scenarioId);
    }

    @Override
    public Set<UUID> findScenarioIdsWithDisabledMissions(Set<UUID> scenarioIds) {
        if (scenarioIds.isEmpty()) return Set.of();
        return jpaRepository.findScenarioIdsWithDisabledMissions(scenarioIds);
    }

    @Override
    public boolean isPreviousMissionCompleted(UUID userId, UUID scenarioId, int orderIndex) {
        return jpaRepository.findByScenario_IdAndOrderIndex(scenarioId, orderIndex)
                .map(mission -> progressJpaRepository.existsByUserIdAndMissionIdAndCompleted(userId, mission.getId(), true))
                .orElse(false);
    }

    @Override
    public int countByScenarioId(UUID scenarioId) {
        return jpaRepository.countByScenario_Id(scenarioId);
    }

    @Override
    public int countByScenarioIdAndEnabledTrue(UUID scenarioId) {
        return jpaRepository.countByScenario_IdAndEnabledTrue(scenarioId);
    }

    @Override
    @Transactional
    public Mission save(Mission mission) {
        ScenarioJpaEntity scenarioEntity = null;
        if (mission.getScenarioId() != null) {
            scenarioEntity = scenarioJpaRepository.findById(mission.getScenarioId()).orElse(null);
        }
        MissionJpaEntity entity = mapper.toJpa(mission, scenarioEntity);

        ThemeJpaEntity themeEntity = themeJpaRepository.findByName(mission.getTheme().getName())
                .orElseThrow(() -> new IllegalArgumentException("Theme not found: " + mission.getTheme()));
        entity.setTheme(themeEntity);

        Set<String> techniqueNames = mission.getTechniques().stream()
                .map(Technique::getName)
                .collect(Collectors.toSet());
        List<TechniqueJpaEntity> found = techniqueJpaRepository.findByNameIn(techniqueNames);
        if (found.size() != techniqueNames.size()) {
            Set<String> foundNames = found.stream().map(TechniqueJpaEntity::getName).collect(Collectors.toSet());
            techniqueNames.removeAll(foundNames);
            throw new IllegalArgumentException("Techniques not found: " + techniqueNames);
        }
        entity.setTechniques(new HashSet<>(found));

        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional
    public int setEnabledByScenarioId(UUID scenarioId, boolean enabled) {
        return jpaRepository.setEnabledByScenarioId(scenarioId, enabled);
    }

    @Override
    @Transactional
    public void setOrderIndex(UUID id, int orderIndex) {
        jpaRepository.setOrderIndex(id, orderIndex);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
