package com.sqlab.infrastructure.adapter.out.persistence.mapper;

import com.sqlab.domain.model.Scenario;
import com.sqlab.domain.model.Theme;
import com.sqlab.infrastructure.adapter.out.persistence.entity.ScenarioJpaEntity;
import com.sqlab.infrastructure.adapter.out.persistence.entity.ThemeJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ScenarioMapper {

    public Scenario toDomain(ScenarioJpaEntity entity) {
        ThemeJpaEntity themeEntity = entity.getTheme();
        Theme theme = new Theme(themeEntity.getId(), themeEntity.getName(),
                themeEntity.getDescription(), themeEntity.getEmoji());
        return new Scenario(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                theme,
                entity.getRequiredLevel(),
                entity.isEnabled()
        );
    }

    public ScenarioJpaEntity toJpa(Scenario domain, ThemeJpaEntity theme) {
        ScenarioJpaEntity entity = new ScenarioJpaEntity();
        entity.setId(domain.getId());
        entity.setTitle(domain.getTitle());
        entity.setDescription(domain.getDescription());
        entity.setTheme(theme);
        entity.setEnabled(domain.isEnabled());
        entity.setRequiredLevel(domain.getRequiredLevel());
        return entity;
    }
}
