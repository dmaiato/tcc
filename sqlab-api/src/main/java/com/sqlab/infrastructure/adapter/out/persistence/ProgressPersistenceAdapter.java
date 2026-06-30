package com.sqlab.infrastructure.adapter.out.persistence;

import com.sqlab.application.port.out.ProgressRepository;
import com.sqlab.domain.model.Progress;
import com.sqlab.infrastructure.adapter.out.persistence.entity.MissionJpaEntity;
import com.sqlab.infrastructure.adapter.out.persistence.entity.ProgressJpaEntity;
import com.sqlab.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import com.sqlab.infrastructure.adapter.out.persistence.mapper.ProgressMapper;
import com.sqlab.infrastructure.adapter.out.persistence.repository.MissionJpaRepository;
import com.sqlab.infrastructure.adapter.out.persistence.repository.ProgressJpaRepository;
import com.sqlab.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Transactional
public class ProgressPersistenceAdapter implements ProgressRepository {

    private final ProgressJpaRepository jpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final MissionJpaRepository missionJpaRepository;
    private final ProgressMapper mapper;

    public ProgressPersistenceAdapter(ProgressJpaRepository jpaRepository,
                                      UserJpaRepository userJpaRepository,
                                      MissionJpaRepository missionJpaRepository,
                                      ProgressMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.userJpaRepository = userJpaRepository;
        this.missionJpaRepository = missionJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Progress save(Progress progress) {
        UserJpaEntity user = userJpaRepository.findById(progress.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + progress.getUserId()));
        MissionJpaEntity mission = missionJpaRepository.findById(progress.getMissionId())
                .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + progress.getMissionId()));

        ProgressJpaEntity entity = mapper.toJpa(progress, user, mission);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<Progress> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByUserIdAndMissionId(UUID userId, UUID missionId) {
        return jpaRepository.existsByUserIdAndMissionId(userId, missionId);
    }

    @Override
    public List<Progress> findCompletedByUserId(UUID userId) {
        return jpaRepository.findByUserIdAndCompleted(userId, true).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Set<UUID> findCompletedMissionIdsByUserId(UUID userId) {
        return jpaRepository.findByUserIdAndCompleted(userId, true).stream()
                .map(e -> e.getMissionId())
                .collect(Collectors.toSet());
    }
}
