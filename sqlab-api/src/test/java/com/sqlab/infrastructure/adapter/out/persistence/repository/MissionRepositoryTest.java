package com.sqlab.infrastructure.adapter.out.persistence.repository;

import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.infrastructure.adapter.out.persistence.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class MissionRepositoryTest extends AbstractPersistenceTest {

    @Autowired private MissionJpaRepository missionRepository;
    @Autowired private ThemeJpaRepository themeRepository;
    @Autowired private TechniqueJpaRepository techniqueRepository;
    @Autowired private ScenarioJpaRepository scenarioRepository;

    private ThemeJpaEntity theme;
    private TechniqueJpaEntity technique;

    @BeforeEach
    void setUp() {
        theme = themeRepository.save(ThemeJpaEntity.builder()
                .name("ASTRONOMY").description("Stars").emoji("⭐").build());
        technique = techniqueRepository.save(TechniqueJpaEntity.builder()
                .name("SELECT").build());
    }

    private MissionJpaEntity createMission(String title, DifficultyLevel difficulty, boolean enabled) {
        return MissionJpaEntity.builder()
                .title(title).briefing("B").objective("O").ddlScript("DDL")
                .xpReward(100)
                .expectedResult(List.of(Map.of("id", 1)))
                .ordered(false)
                .theme(theme)
                .difficulty(difficulty)
                .enabled(enabled)
                .techniques(Set.of(technique))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void saveAndFindById() {
        var saved = missionRepository.save(createMission("M1", DifficultyLevel.BEGINNER, true));
        var found = missionRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("M1");
    }

    @Test
    void findByThemeName() {
        missionRepository.save(createMission("M1", DifficultyLevel.BEGINNER, true));
        var results = missionRepository.findByTheme_Name("ASTRONOMY");
        assertThat(results).isNotEmpty();
    }

    @Test
    void findByDifficulty() {
        missionRepository.save(createMission("M1", DifficultyLevel.ADVANCED, true));
        var results = missionRepository.findByDifficulty(DifficultyLevel.ADVANCED);
        assertThat(results).isNotEmpty();
    }

    @Test
    void findByThemeAndDifficulty() {
        missionRepository.save(createMission("M1", DifficultyLevel.BEGINNER, true));
        var results = missionRepository.findByTheme_NameAndDifficulty("ASTRONOMY", DifficultyLevel.BEGINNER);
        assertThat(results).isNotEmpty();
    }

    @Test
    void findByEnabledTrue() {
        missionRepository.save(createMission("Enabled1", DifficultyLevel.BEGINNER, true));
        missionRepository.save(createMission("Enabled2", DifficultyLevel.INTERMEDIATE, true));
        missionRepository.save(createMission("Disabled", DifficultyLevel.BEGINNER, false));
        var results = missionRepository.findByEnabledTrue();
        assertThat(results).hasSize(2);
    }

    @Test
    void findByScenarioIdOrderByOrderIndex() {
        var scenario = scenarioRepository.save(ScenarioJpaEntity.builder()
                .title("S1").description("D").theme(theme)
                .enabled(true).requiredLevel(1)
                .createdAt(LocalDateTime.now()).build());

        var m1 = missionRepository.save(MissionJpaEntity.builder()
                .title("M1").briefing("B").objective("O").ddlScript("DDL")
                .xpReward(10).expectedResult(List.of(Map.of("x", 1)))
                .ordered(false).theme(theme).difficulty(DifficultyLevel.BEGINNER)
                .enabled(true).scenario(scenario).orderIndex(2)
                .techniques(Set.of(technique)).createdAt(LocalDateTime.now()).build());
        var m2 = missionRepository.save(MissionJpaEntity.builder()
                .title("M2").briefing("B").objective("O").ddlScript("DDL")
                .xpReward(10).expectedResult(List.of(Map.of("x", 1)))
                .ordered(false).theme(theme).difficulty(DifficultyLevel.BEGINNER)
                .enabled(true).scenario(scenario).orderIndex(1)
                .techniques(Set.of(technique)).createdAt(LocalDateTime.now()).build());

        var results = missionRepository.findByScenario_IdOrderByOrderIndex(scenario.getId()).toList();
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getTitle()).isEqualTo("M2");
        assertThat(results.get(1).getTitle()).isEqualTo("M1");
    }

    @Test
    void countByScenarioId() {
        var scenario = scenarioRepository.save(ScenarioJpaEntity.builder()
                .title("S").description("D").theme(theme)
                .enabled(true).requiredLevel(1)
                .createdAt(LocalDateTime.now()).build());
        missionRepository.save(MissionJpaEntity.builder()
                .title("M1").briefing("B").objective("O").ddlScript("DDL")
                .xpReward(10).expectedResult(List.of(Map.of("x", 1)))
                .ordered(false).theme(theme).difficulty(DifficultyLevel.BEGINNER)
                .enabled(true).scenario(scenario).orderIndex(1)
                .techniques(Set.of()).createdAt(LocalDateTime.now()).build());
        missionRepository.save(MissionJpaEntity.builder()
                .title("M2").briefing("B").objective("O").ddlScript("DDL")
                .xpReward(10).expectedResult(List.of(Map.of("x", 1)))
                .ordered(false).theme(theme).difficulty(DifficultyLevel.BEGINNER)
                .enabled(true).scenario(scenario).orderIndex(2)
                .techniques(Set.of()).createdAt(LocalDateTime.now()).build());

        assertThat(missionRepository.countByScenario_Id(scenario.getId())).isEqualTo(2);
    }

    @Test
    void techniquesArePersisted() {
        var technique2 = techniqueRepository.save(TechniqueJpaEntity.builder().name("JOIN").build());
        var mission = createMission("WithTech", DifficultyLevel.BEGINNER, true);
        mission.setTechniques(Set.of(technique, technique2));
        var saved = missionRepository.save(mission);

        var found = missionRepository.findById(saved.getId()).get();
        assertThat(found.getTechniques()).hasSize(2);
    }
}
