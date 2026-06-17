package com.sqlab.infrastructure.adapter.out.persistence.repository;

import com.sqlab.infrastructure.adapter.out.persistence.entity.ScenarioJpaEntity;
import com.sqlab.infrastructure.adapter.out.persistence.entity.ThemeJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class ScenarioRepositoryTest extends AbstractPersistenceTest {

    @Autowired private ScenarioJpaRepository scenarioRepository;
    @Autowired private ThemeJpaRepository themeRepository;

    private ThemeJpaEntity theme;

    @BeforeEach
    void setUp() {
        theme = themeRepository.save(ThemeJpaEntity.builder()
                .name("ASTRONOMY").description("Stars").emoji("⭐").build());
    }

    @Test
    void saveAndFindById() {
        var scenario = scenarioRepository.save(ScenarioJpaEntity.builder()
                .title("S1").description("Desc").theme(theme)
                .enabled(true).requiredLevel(1)
                .createdAt(LocalDateTime.now()).build());

        var found = scenarioRepository.findById(scenario.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("S1");
    }

    @Test
    void findAll() {
        scenarioRepository.save(ScenarioJpaEntity.builder()
                .title("S1").description("D1").theme(theme)
                .enabled(true).requiredLevel(1)
                .createdAt(LocalDateTime.now()).build());
        scenarioRepository.save(ScenarioJpaEntity.builder()
                .title("S2").description("D2").theme(theme)
                .enabled(false).requiredLevel(2)
                .createdAt(LocalDateTime.now()).build());

        var results = scenarioRepository.findAll();
        assertThat(results).hasSize(2);
    }

    @Test
    void findByIdReturnsEmptyForUnknown() {
        assertThat(scenarioRepository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void foreignKeyConstraintOnTheme() {
        var invalidTheme = ThemeJpaEntity.builder()
                .id(UUID.randomUUID()).name("INVALID").build();
        assertThatThrownBy(() -> scenarioRepository.saveAndFlush(ScenarioJpaEntity.builder()
                .title("Bad").description("Bad").theme(invalidTheme)
                .enabled(true).requiredLevel(1)
                .createdAt(LocalDateTime.now()).build()))
                .isInstanceOfAny(DataIntegrityViolationException.class,
                        org.springframework.dao.InvalidDataAccessApiUsageException.class);
    }
}
