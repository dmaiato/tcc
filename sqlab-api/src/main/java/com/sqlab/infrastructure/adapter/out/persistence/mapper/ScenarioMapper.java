package com.sqlab.infrastructure.adapter.out.persistence.mapper;

import com.sqlab.domain.model.Scenario;
import com.sqlab.infrastructure.adapter.out.persistence.entity.ScenarioJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ScenarioMapper {

    public Scenario toDomain(ScenarioJpaEntity entity) {
        return new Scenario(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getTheme()
        );
    }
}
