package com.sqlab.infrastructure.adapter.out.persistence.repository;

import com.sqlab.domain.model.UserRole;
import com.sqlab.infrastructure.adapter.out.persistence.entity.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class JpaEntityMappingsTest extends AbstractPersistenceTest {

    @Autowired private EntityManager em;
    @Autowired private ThemeJpaRepository themeRepository;
    @Autowired private TechniqueJpaRepository techniqueRepository;
    @Autowired private ScenarioJpaRepository scenarioRepository;
    @Autowired private MissionJpaRepository missionRepository;
    @Autowired private UserJpaRepository userRepository;
    @Autowired private ProgressJpaRepository progressRepository;

    @Test
    void persistTheme() {
        var theme = themeRepository.save(ThemeJpaEntity.builder()
                .name("BIOLOGY").description("Life").emoji("🧬").build());
        em.flush();
        em.clear();

        var found = em.find(ThemeJpaEntity.class, theme.getId());
        assertThat(found.getName()).isEqualTo("BIOLOGY");
    }

    @Test
    void persistTechnique() {
        var technique = techniqueRepository.save(TechniqueJpaEntity.builder().name("GROUP BY").build());
        em.flush();
        em.clear();

        var found = em.find(TechniqueJpaEntity.class, technique.getId());
        assertThat(found.getName()).isEqualTo("GROUP BY");
    }

    @Test
    void persistFullMissionChain() {
        var theme = themeRepository.save(ThemeJpaEntity.builder()
                .name("ASTRONOMY").build());
        var technique = techniqueRepository.save(TechniqueJpaEntity.builder().name("SELECT").build());
        var scenario = scenarioRepository.save(ScenarioJpaEntity.builder()
                .title("S1").description("D").theme(theme)
                .enabled(true).requiredLevel(1)
                .createdAt(LocalDateTime.now()).build());

        var mission = MissionJpaEntity.builder()
                .title("M1").briefing("B").objective("O").ddlScript("DDL")
                .xpReward(50)
                .expectedResult(List.of(Map.of("id", 1)))
                .ordered(true).theme(theme).difficulty(com.sqlab.domain.model.DifficultyLevel.BEGINNER)
                .enabled(true).scenario(scenario).orderIndex(1)
                .techniques(Set.of(technique))
                .createdAt(LocalDateTime.now()).build();
        missionRepository.save(mission);
        em.flush();
        em.clear();

        var found = em.find(MissionJpaEntity.class, mission.getId());
        assertThat(found.getTheme().getName()).isEqualTo("ASTRONOMY");
        assertThat(found.getScenario().getTitle()).isEqualTo("S1");
        assertThat(found.getTechniques()).hasSize(1);
        assertThat(found.getDifficulty()).isEqualTo(com.sqlab.domain.model.DifficultyLevel.BEGINNER);
    }

    @Test
    void persistProgress() {
        var theme = themeRepository.save(ThemeJpaEntity.builder().name("ASTRONOMY").build());
        var user = userRepository.save(UserJpaEntity.builder()
                .username("jpatest").email("jpa@test.com").passwordHash("h")
                .xp(0).role(UserRole.USER).createdAt(LocalDateTime.now()).build());
        var mission = missionRepository.save(MissionJpaEntity.builder()
                .title("JPAM").briefing("B").objective("O").ddlScript("DDL")
                .xpReward(10).expectedResult(List.of(Map.of("x", 1)))
                .ordered(false).theme(theme).difficulty(com.sqlab.domain.model.DifficultyLevel.BEGINNER)
                .enabled(true).techniques(Set.of())
                .createdAt(LocalDateTime.now()).build());

        var progress = ProgressJpaEntity.builder()
                .user(user).mission(mission).completed(true)
                .completedAt(LocalDateTime.now()).createdAt(LocalDateTime.now()).build();
        progressRepository.save(progress);
        em.flush();
        em.clear();

        var found = em.find(ProgressJpaEntity.class, progress.getId());
        assertThat(found.getUser().getId()).isEqualTo(user.getId());
        assertThat(found.getMission().getId()).isEqualTo(mission.getId());
        assertThat(found.isCompleted()).isTrue();
    }
}
