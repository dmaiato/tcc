package com.sqlab.infrastructure.adapter.out.persistence.repository;

import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.infrastructure.adapter.out.persistence.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class ProgressRepositoryTest extends AbstractPersistenceTest {

    @Autowired private ProgressJpaRepository progressRepository;
    @Autowired private UserJpaRepository userRepository;
    @Autowired private MissionJpaRepository missionRepository;
    @Autowired private ThemeJpaRepository themeRepository;
    @Autowired private TechniqueJpaRepository techniqueRepository;

    private UserJpaEntity user;
    private MissionJpaEntity mission;
    private ThemeJpaEntity theme;

    @BeforeEach
    void setUp() {
        theme = themeRepository.save(ThemeJpaEntity.builder()
                .name("ASTRONOMY").description("Stars").emoji("⭐").build());
        var technique = techniqueRepository.save(TechniqueJpaEntity.builder().name("SELECT").build());
        user = userRepository.save(UserJpaEntity.builder()
                .username("puser").email("p@test.com").passwordHash("h")
                .xp(0).role("USER").createdAt(LocalDateTime.now()).build());
        mission = missionRepository.save(MissionJpaEntity.builder()
                .title("PM").briefing("B").objective("O").ddlScript("DDL")
                .xpReward(10).expectedResult(List.of(Map.of("x", 1)))
                .ordered(false).theme(theme).difficulty(DifficultyLevel.BEGINNER)
                .enabled(true).techniques(Set.of(technique))
                .createdAt(LocalDateTime.now()).build());
    }

    @Test
    void saveAndFindByUserId() {
        var prog = ProgressJpaEntity.builder()
                .user(user).mission(mission)
                .completed(true).completedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now()).build();
        progressRepository.save(prog);

        var results = progressRepository.findByUserId(user.getId());
        assertThat(results).hasSize(1);
        assertThat(results.get(0).isCompleted()).isTrue();
    }

    @Test
    void findByUserIdAndCompleted() {
        var mission2 = missionRepository.save(MissionJpaEntity.builder()
                .title("PM2").briefing("B").objective("O").ddlScript("DDL")
                .xpReward(10).expectedResult(List.of(Map.of("x", 1)))
                .ordered(false).theme(theme).difficulty(DifficultyLevel.BEGINNER)
                .enabled(true).techniques(Set.of())
                .createdAt(LocalDateTime.now()).build());

        progressRepository.save(ProgressJpaEntity.builder()
                .user(user).mission(mission).completed(true)
                .completedAt(LocalDateTime.now()).createdAt(LocalDateTime.now()).build());
        progressRepository.save(ProgressJpaEntity.builder()
                .user(user).mission(mission2).completed(false)
                .createdAt(LocalDateTime.now()).build());

        var completed = progressRepository.findByUserIdAndCompleted(user.getId(), true);
        assertThat(completed).hasSize(1);
    }

    @Test
    void existsByUserIdAndMissionId() {
        progressRepository.save(ProgressJpaEntity.builder()
                .user(user).mission(mission).completed(true)
                .completedAt(LocalDateTime.now()).createdAt(LocalDateTime.now()).build());
        assertThat(progressRepository.existsByUserIdAndMissionId(user.getId(), mission.getId())).isTrue();
        assertThat(progressRepository.existsByUserIdAndMissionId(user.getId(), UUID.randomUUID())).isFalse();
    }

    @Test
    void uniqueConstraintOnUserAndMission() {
        progressRepository.save(ProgressJpaEntity.builder()
                .user(user).mission(mission).completed(true)
                .completedAt(LocalDateTime.now()).createdAt(LocalDateTime.now()).build());
        assertThatThrownBy(() -> progressRepository.saveAndFlush(ProgressJpaEntity.builder()
                .user(user).mission(mission).completed(false)
                .createdAt(LocalDateTime.now()).build()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
