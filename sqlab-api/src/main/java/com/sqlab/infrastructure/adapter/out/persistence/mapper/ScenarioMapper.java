package com.sqlab.infrastructure.adapter.out.persistence.mapper;

import com.sqlab.domain.model.Scenario;
import com.sqlab.infrastructure.adapter.out.persistence.entity.ScenarioJpaEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ScenarioMapper {

    public Scenario toDomain(ScenarioJpaEntity entity) {
        return new Scenario(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getTheme(),
                entity.isEnabled(),
                entity.getRequiredLevel()
        );
    }

    public ScenarioJpaEntity toJpa(Scenario domain) {
        return ScenarioJpaEntity.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .theme(domain.getTheme())
                .enabled(domain.isEnabled())
                .requiredLevel(domain.getRequiredLevel())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
