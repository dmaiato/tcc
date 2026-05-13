package com.sqlab.infrastructure.adapter.out.persistence;

import com.sqlab.application.port.out.ProgressRepository;
import com.sqlab.domain.model.Progress;
import com.sqlab.infrastructure.adapter.out.persistence.entity.ProgressJpaEntity;
import com.sqlab.infrastructure.adapter.out.persistence.repository.ProgressJpaRepository;
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

    public ProgressPersistenceAdapter(ProgressJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Progress save(Progress progress) {
        ProgressJpaEntity entity = ProgressJpaEntity.builder()
                .id(progress.getId())
                .userId(progress.getUserId())
                .missionId(progress.getMissionId())
                .completed(progress.isCompleted())
                .completedAt(progress.getCompletedAt())
                .build();

        ProgressJpaEntity saved = jpaRepository.save(entity);

        return new Progress(
                saved.getId(),
                saved.getUserId(),
                saved.getMissionId(),
                saved.isCompleted(),
                saved.getCompletedAt()
        );
    }

    @Override
    public List<Progress> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(e -> new Progress(
                        e.getId(),
                        e.getUserId(),
                        e.getMissionId(),
                        e.isCompleted(),
                        e.getCompletedAt()
                ))
                .toList();
    }

    @Override
    public boolean existsByUserIdAndMissionId(UUID userId, UUID missionId) {
        return jpaRepository.existsByUserIdAndMissionId(userId, missionId);
    }

    @Override
    public List<Progress> findCompletedByUserId(UUID userId) {
        return jpaRepository.findByUserIdAndCompleted(userId, true).stream()
                .map(e -> new Progress(
                        e.getId(),
                        e.getUserId(),
                        e.getMissionId(),
                        e.isCompleted(),
                        e.getCompletedAt()
                ))
                .toList();
    }

    @Override
    public Set<UUID> findCompletedMissionIdsByUserId(UUID userId) {
        return jpaRepository.findByUserIdAndCompleted(userId, true).stream()
                .map(ProgressJpaEntity::getMissionId)
                .collect(Collectors.toSet());
    }
}