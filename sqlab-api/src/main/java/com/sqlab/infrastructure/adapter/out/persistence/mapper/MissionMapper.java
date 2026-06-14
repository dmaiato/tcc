package com.sqlab.infrastructure.adapter.out.persistence.mapper;

import com.sqlab.domain.model.ExpectedTuple;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Theme;
import com.sqlab.infrastructure.adapter.out.persistence.entity.MissionJpaEntity;
import com.sqlab.infrastructure.adapter.out.persistence.entity.TechniqueJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MissionMapper {

    public Mission toDomain(MissionJpaEntity entity) {
        int requiredLevel = entity.getScenario() != null ? entity.getScenario().getRequiredLevel() : 0;
        return new Mission(
                entity.getId(),
                entity.getTitle(),
                entity.getBriefing(),
                entity.getObjective(),
                entity.getHint(),
                entity.getDdlScript(),
                entity.getDmlScript(),
                entity.getTechniques().stream().map(TechniqueJpaEntity::getName).sorted().toList(),
                entity.getXpReward(),
                new ExpectedTuple(entity.getExpectedResult()),
                entity.isOrdered(),
                Theme.valueOf(entity.getTheme().getName()),
                entity.getDifficulty(),
                entity.getScenario() != null ? entity.getScenario().getId() : null,
                entity.getOrderIndex(),
                entity.isEnabled(),
                requiredLevel
        );
    }

    public MissionJpaEntity toJpa(Mission domain) {
        MissionJpaEntity entity = new MissionJpaEntity();
        entity.setId(domain.getId());
        entity.setTitle(domain.getTitle());
        entity.setBriefing(domain.getBriefing());
        entity.setObjective(domain.getObjective());
        entity.setHint(domain.getHint());
        entity.setDdlScript(domain.getDdlScript());
        entity.setDmlScript(domain.getDmlScript());
        entity.setXpReward(domain.getXpReward());
        entity.setExpectedResult(domain.getExpectedResult().rows());
        entity.setOrdered(domain.isOrdered());
        entity.setDifficulty(domain.getDifficulty());
        entity.setEnabled(domain.isEnabled());
        entity.setOrderIndex(domain.getOrderIndex());
        return entity;
    }
}
