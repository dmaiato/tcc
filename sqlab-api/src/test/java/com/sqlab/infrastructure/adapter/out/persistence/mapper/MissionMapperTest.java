package com.sqlab.infrastructure.adapter.out.persistence.mapper;

import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Theme;
import com.sqlab.infrastructure.adapter.out.persistence.entity.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MissionMapperTest {

    private final MissionMapper mapper = new MissionMapper();
    private final UUID id = UUID.randomUUID();
    private final LocalDateTime now = LocalDateTime.now();

    private ThemeJpaEntity themeJpa() {
        return ThemeJpaEntity.builder().id(UUID.randomUUID()).name("ASTRONOMY").build();
    }

    private TechniqueJpaEntity technique(String name) {
        return TechniqueJpaEntity.builder().id(UUID.randomUUID()).name(name).build();
    }

    @Test
    void toDomainWithFullEntity() {
        var scenario = ScenarioJpaEntity.builder()
                .id(UUID.randomUUID()).title("S1").description("D")
                .theme(themeJpa()).enabled(true).requiredLevel(3)
                .createdAt(now).build();

        var entity = MissionJpaEntity.builder()
                .id(id)
                .title("Mission X")
                .briefing("Briefing")
                .objective("Objective")
                .hint("Hint")
                .ddlScript("CREATE TABLE...")
                .dmlScript("INSERT...")
                .xpReward(100)
                .expectedResult(List.of(Map.of("id", 1)))
                .ordered(true)
                .theme(themeJpa())
                .difficulty(DifficultyLevel.ADVANCED)
                .enabled(true)
                .scenario(scenario)
                .orderIndex(2)
                .techniques(Set.of(technique("SELECT"), technique("JOIN")))
                .createdAt(now)
                .build();

        var domain = mapper.toDomain(entity);

        assertEquals(id, domain.getId());
        assertEquals("Mission X", domain.getTitle());
        assertEquals(100, domain.getXpReward());
        assertTrue(domain.isOrdered());
        assertEquals(Theme.ASTRONOMY, domain.getTheme());
        assertEquals(DifficultyLevel.ADVANCED, domain.getDifficulty());
        assertEquals(scenario.getId(), domain.getScenarioId());
        assertEquals(Integer.valueOf(2), domain.getOrderIndex());
        assertTrue(domain.isEnabled());
        assertEquals(3, domain.getRequiredLevel());
        assertTrue(domain.getTechniques().contains("JOIN"));
        assertTrue(domain.getTechniques().contains("SELECT"));
        assertEquals(2, domain.getTechniques().size());
    }

    @Test
    void toDomainWithoutScenario() {
        var entity = MissionJpaEntity.builder()
                .id(id)
                .title("Mission X")
                .briefing("B")
                .objective("O")
                .ddlScript("DDL")
                .xpReward(50)
                .expectedResult(List.of(Map.of("x", 1)))
                .ordered(false)
                .theme(themeJpa())
                .difficulty(DifficultyLevel.BEGINNER)
                .enabled(true)
                .techniques(Set.of())
                .createdAt(now)
                .build();

        var domain = mapper.toDomain(entity);

        assertNull(domain.getScenarioId());
        assertNull(domain.getOrderIndex());
        assertEquals(0, domain.getRequiredLevel());
    }

    @Test
    void toDomainTechniquesAreSortedAlphabetically() {
        var entity = MissionJpaEntity.builder()
                .id(id)
                .title("T")
                .briefing("B")
                .objective("O")
                .ddlScript("DDL")
                .xpReward(10)
                .expectedResult(List.of(Map.of("x", 1)))
                .ordered(false)
                .theme(themeJpa())
                .difficulty(DifficultyLevel.BEGINNER)
                .enabled(true)
                .techniques(Set.of(technique("ZEBRA"), technique("ALPHA"), technique("BETA")))
                .createdAt(now)
                .build();

        var domain = mapper.toDomain(entity);

        assertEquals(List.of("ALPHA", "BETA", "ZEBRA"), domain.getTechniques());
    }

    @Test
    void toJpaMapsBasicFields() {
        var domain = Mission.builder()
                .id(id)
                .title("Mission")
                .briefing("B")
                .objective("O")
                .hint("H")
                .ddlScript("DDL")
                .dmlScript("DML")
                .techniques(List.of("SELECT"))
                .xpReward(75)
                .expectedResult(new com.sqlab.domain.model.ExpectedTuple(List.of(Map.of("v", 1))))
                .ordered(false)
                .theme(Theme.ASTRONOMY)
                .difficulty(DifficultyLevel.INTERMEDIATE)
                .scenarioId(null)
                .orderIndex(null)
                .enabled(true)
                .requiredLevel(0)
                .build();

        var entity = mapper.toJpa(domain);

        assertEquals(id, entity.getId());
        assertEquals("Mission", entity.getTitle());
        assertEquals(75, entity.getXpReward());
        assertEquals(List.of(Map.of("v", 1)), entity.getExpectedResult());
        assertFalse(entity.isOrdered());
        assertEquals(DifficultyLevel.INTERMEDIATE, entity.getDifficulty());
        assertTrue(entity.isEnabled());
        assertNull(entity.getOrderIndex());
    }

    @Test
    void toJpaMapsOrderIndex() {
        var domain = Mission.builder()
                .id(id)
                .title("M")
                .briefing("B")
                .objective("O")
                .ddlScript("DDL")
                .techniques(List.of())
                .xpReward(10)
                .expectedResult(new com.sqlab.domain.model.ExpectedTuple(List.of(Map.of("x", 1))))
                .ordered(false)
                .theme(Theme.ASTRONOMY)
                .difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(UUID.randomUUID())
                .orderIndex(1)
                .enabled(true)
                .requiredLevel(0)
                .build();

        var entity = mapper.toJpa(domain);

        assertEquals(Integer.valueOf(1), entity.getOrderIndex());
    }

    @Test
    void toJpaDoesNotSetThemeOrScenario() {
        var domain = Mission.builder()
                .id(id).title("M").briefing("B").objective("O")
                .ddlScript("DDL")
                .techniques(List.of())
                .xpReward(10)
                .expectedResult(new com.sqlab.domain.model.ExpectedTuple(List.of(Map.of("x", 1))))
                .ordered(false).theme(Theme.ASTRONOMY)
                .difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(UUID.randomUUID()).orderIndex(1).enabled(true).requiredLevel(0)
                .build();

        var entity = mapper.toJpa(domain);

        assertNull(entity.getTheme(), "theme é setado pelo adapter, não pelo mapper");
        assertNull(entity.getScenario(), "scenario é setado pelo adapter, não pelo mapper");
        assertTrue(entity.getTechniques().isEmpty(), "techniques são setadas pelo adapter");
    }
}
