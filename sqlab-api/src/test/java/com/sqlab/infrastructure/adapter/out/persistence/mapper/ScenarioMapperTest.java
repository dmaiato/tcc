package com.sqlab.infrastructure.adapter.out.persistence.mapper;

import com.sqlab.domain.model.Scenario;
import com.sqlab.domain.model.Theme;
import com.sqlab.infrastructure.adapter.out.persistence.entity.ScenarioJpaEntity;
import com.sqlab.infrastructure.adapter.out.persistence.entity.ThemeJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ScenarioMapperTest {

    private final ScenarioMapper mapper = new ScenarioMapper();
    private final UUID id = UUID.randomUUID();
    private final LocalDateTime now = LocalDateTime.now();

    private ThemeJpaEntity themeJpa() {
        return ThemeJpaEntity.builder().id(UUID.randomUUID()).name("ASTRONOMY").build();
    }

    @Test
    void toDomain() {
        var entity = ScenarioJpaEntity.builder()
                .id(id)
                .title("SQL Basics")
                .description("Learn SQL")
                .theme(themeJpa())
                .enabled(true)
                .requiredLevel(2)
                .createdAt(now)
                .build();

        var domain = mapper.toDomain(entity);

        assertEquals(id, domain.getId());
        assertEquals("SQL Basics", domain.getTitle());
        assertEquals("Learn SQL", domain.getDescription());
        assertEquals(new Theme(UUID.randomUUID(), "ASTRONOMY", null, null), domain.getTheme());
        assertTrue(domain.isEnabled());
        assertEquals(2, domain.getRequiredLevel());
    }

    @Test
    void toDomainDisabled() {
        var entity = ScenarioJpaEntity.builder()
                .id(id)
                .title("Advanced")
                .description("Advanced SQL")
                .theme(themeJpa())
                .enabled(false)
                .requiredLevel(5)
                .createdAt(now)
                .build();

        var domain = mapper.toDomain(entity);

        assertFalse(domain.isEnabled());
        assertEquals(5, domain.getRequiredLevel());
    }

    @Test
    void toJpa() {
        var theme = themeJpa();
        var domain = new Scenario(id, "Title", "Desc", new Theme(UUID.randomUUID(), "ASTRONOMY", null, null), true, 3);

        var entity = mapper.toJpa(domain, theme);

        assertEquals(id, entity.getId());
        assertEquals("Title", entity.getTitle());
        assertEquals("Desc", entity.getDescription());
        assertSame(theme, entity.getTheme());
        assertTrue(entity.isEnabled());
        assertEquals(3, entity.getRequiredLevel());
    }
}
