package com.sqlab.infrastructure.adapter.out.persistence.mapper;

import com.sqlab.domain.model.ExpectedTuple;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Technique;
import com.sqlab.domain.model.Theme;
import com.sqlab.infrastructure.adapter.out.persistence.entity.MissionJpaEntity;
import com.sqlab.infrastructure.adapter.out.persistence.entity.ScenarioJpaEntity;
import com.sqlab.infrastructure.adapter.out.persistence.entity.TechniqueJpaEntity;
import com.sqlab.infrastructure.adapter.out.persistence.entity.ThemeJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class MissionMapper {

    public Mission toDomain(MissionJpaEntity entity) {
        int requiredLevel = entity.getScenario() != null ? entity.getScenario().getRequiredLevel() : 0;
        ThemeJpaEntity themeEntity = entity.getTheme();
        Theme theme = new Theme(themeEntity.getId(), themeEntity.getName(),
                themeEntity.getDescription(), themeEntity.getEmoji());
        return new Mission(
                entity.getId(),
                entity.getTitle(),
                entity.getBriefing(),
                entity.getObjective(),
                entity.getHint(),
                entity.getDdlScript(),
                entity.getDmlScript(),
                entity.getTechniques().stream()
                        .map(t -> new Technique(t.getId(), t.getName()))
                        .sorted(Comparator.comparing(Technique::getName))
                        .toList(),
                entity.getXpReward(),
                new ExpectedTuple(entity.getExpectedResult()),
                entity.isOrdered(),
                theme,
                entity.getDifficulty(),
                entity.getScenario() != null ? entity.getScenario().getId() : null,
                entity.getOrderIndex(),
                entity.isEnabled(),
                requiredLevel
        );
    }

    public MissionJpaEntity toJpa(Mission domain, ScenarioJpaEntity scenarioEntity) {
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
        entity.setScenario(scenarioEntity);
        return entity;
    }
}
