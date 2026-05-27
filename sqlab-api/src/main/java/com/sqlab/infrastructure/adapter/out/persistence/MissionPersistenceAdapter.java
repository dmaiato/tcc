package com.sqlab.infrastructure.adapter.out.persistence;

import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Theme;
import com.sqlab.infrastructure.adapter.out.persistence.entity.MissionJpaEntity;
import com.sqlab.infrastructure.adapter.out.persistence.mapper.MissionMapper;
import com.sqlab.infrastructure.adapter.out.persistence.repository.MissionJpaRepository;
import com.sqlab.infrastructure.adapter.out.persistence.repository.ProgressJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@Transactional(readOnly = true)
public class MissionPersistenceAdapter implements MissionRepository {

    private final MissionJpaRepository jpaRepository;
    private final MissionMapper mapper;
    private final ProgressJpaRepository progressJpaRepository;

    public MissionPersistenceAdapter(MissionJpaRepository jpaRepository, MissionMapper mapper, ProgressJpaRepository progressJpaRepository) {
        this.jpaRepository = jpaRepository;
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
        return jpaRepository.findByTheme(theme).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Mission> findByDifficulty(DifficultyLevel difficulty) {
        return jpaRepository.findByDifficulty(difficulty).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Mission> findByThemeAndDifficulty(Theme theme, DifficultyLevel difficulty) {
        return jpaRepository.findByThemeAndDifficulty(theme, difficulty).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Mission> findByScenarioIdOrderByOrderIndex(UUID scenarioId) {
        return jpaRepository.findByScenarioIdOrderByOrderIndex(scenarioId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Mission> findByEnabledTrue() {
        return jpaRepository.findByEnabledTrue().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean isPreviousMissionCompleted(UUID userId, UUID scenarioId, int orderIndex) {
        return jpaRepository.findByScenarioIdAndOrderIndex(scenarioId, orderIndex)
                .map(mission -> progressJpaRepository.existsByUserIdAndMissionIdAndCompleted(userId, mission.getId(), true))
                .orElse(false);
    }

    @Override
    public int countByScenarioId(UUID scenarioId) {
        return jpaRepository.countByScenarioId(scenarioId);
    }

    @Override
    public int countByScenarioIdAndEnabledTrue(UUID scenarioId) {
        return jpaRepository.countByScenarioIdAndEnabledTrue(scenarioId);
    }

    @Override
    @Transactional
    public Mission save(Mission mission) {
        MissionJpaEntity entity = mapper.toJpa(mission);
        if (mission.getId() != null) {
        jpaRepository.findById(mission.getId()).ifPresent(existing -> {
                entity.setCreatedAt(existing.getCreatedAt());
                entity.setScenario(existing.getScenario());
        });
        }
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}