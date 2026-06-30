package com.sqlab.infrastructure.adapter.out.persistence.mapper;

import com.sqlab.domain.model.Progress;
import com.sqlab.infrastructure.adapter.out.persistence.entity.MissionJpaEntity;
import com.sqlab.infrastructure.adapter.out.persistence.entity.ProgressJpaEntity;
import com.sqlab.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ProgressMapper {

    public Progress toDomain(ProgressJpaEntity entity) {
        return new Progress(
                entity.getId(),
                entity.getUserId(),
                entity.getMissionId(),
                entity.isCompleted(),
                entity.getCompletedAt()
        );
    }

    public ProgressJpaEntity toJpa(Progress domain, UserJpaEntity user, MissionJpaEntity mission) {
        ProgressJpaEntity entity = new ProgressJpaEntity();
        entity.setId(domain.getId());
        entity.setUserId(user.getId());
        entity.setMissionId(mission.getId());
        entity.setCompleted(domain.isCompleted());
        entity.setCompletedAt(domain.getCompletedAt());
        return entity;
    }
}
