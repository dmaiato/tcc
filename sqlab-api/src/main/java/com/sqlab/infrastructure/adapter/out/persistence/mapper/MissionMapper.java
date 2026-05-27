package com.sqlab.infrastructure.adapter.out.persistence.mapper;

import com.sqlab.domain.model.ExpectedTuple;
import com.sqlab.domain.model.Mission;
import com.sqlab.infrastructure.adapter.out.persistence.entity.MissionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class MissionMapper {

    public Mission toDomain(MissionJpaEntity entity) {
        String scenarioTitle = entity.getScenario() != null ? entity.getScenario().getTitle() : null;
        return new Mission(
                entity.getId(),
                entity.getTitle(),
                entity.getBriefing(),
                entity.getObjective(),
                entity.getHint(),
                entity.getDdlScript(),
                entity.getDmlScript(),
                entity.getTechniques(),
                entity.getXpReward(),
                new ExpectedTuple(entity.getExpectedResult()),
                entity.isOrdered(),
                entity.getTheme(),
                entity.getDifficulty(),
                entity.getScenarioId(),
                entity.getOrderIndex(),
                scenarioTitle,
                entity.isEnabled()
        );
    }

    public MissionJpaEntity toJpa(Mission domain) {
        return MissionJpaEntity.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .briefing(domain.getBriefing())
                .objective(domain.getObjective())
                .hint(domain.getHint())
                .ddlScript(domain.getDdlScript())
                .dmlScript(domain.getDmlScript())
                .techniques(domain.getTechniques())
                .xpReward(domain.getXpReward())
                .expectedResult(domain.getExpectedResult().rows())
                .ordered(domain.isOrdered())
                .theme(domain.getTheme())
                .difficulty(domain.getDifficulty())
                .scenarioId(domain.getScenarioId())
                .orderIndex(domain.getOrderIndex())
                .enabled(domain.isEnabled())
                .build();
    }
}