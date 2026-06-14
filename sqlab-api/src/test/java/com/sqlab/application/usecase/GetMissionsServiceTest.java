package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetMissionsUseCase;
import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.application.port.out.UserRepository;
import com.sqlab.domain.exception.LevelRequiredException;
import com.sqlab.domain.exception.MissionLockedException;
import com.sqlab.domain.exception.MissionNotFoundException;
import com.sqlab.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetMissionsServiceTest {

    @Mock private MissionRepository missionRepository;
    @Mock private UserRepository userRepository;
    @Mock private ScenarioRepository scenarioRepository;

    private GetMissionsService service;
    private final UUID userId = UUID.randomUUID();
    private final UUID missionId = UUID.randomUUID();
    private final UUID scenarioId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new GetMissionsService(missionRepository, userRepository, scenarioRepository);
    }

    private Mission createMission(boolean enabled, int requiredLevel) {
        return Mission.builder()
                .id(missionId).title("M").briefing("B").objective("O")
                .ddlScript("DDL").techniques(List.of()).xpReward(10)
                .expectedResult(new ExpectedTuple(List.of(Map.of("x", 1))))
                .ordered(false).theme(Theme.ASTRONOMY)
                .difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(null).orderIndex(null)
                .enabled(enabled).requiredLevel(requiredLevel)
                .build();
    }

    @Test
    void listAllWithoutFilters() {
        var mission = createMission(true, 0);
        when(missionRepository.findByEnabledTrue()).thenReturn(List.of(mission));

        var result = service.handle(new GetMissionsUseCase.ListAllQuery());
        assertThat(result).hasSize(1);
    }

    @Test
    void listWithThemeFilter() {
        var mission = createMission(true, 0);
        when(missionRepository.findByTheme(Theme.ASTRONOMY)).thenReturn(List.of(mission));

        var result = service.handle(new GetMissionsUseCase.ListAllQuery(Theme.ASTRONOMY, null));
        assertThat(result).hasSize(1);
    }

    @Test
    void listWithDifficultyFilter() {
        var mission = createMission(true, 0);
        when(missionRepository.findByDifficulty(DifficultyLevel.BEGINNER)).thenReturn(List.of(mission));

        var result = service.handle(new GetMissionsUseCase.ListAllQuery(null, DifficultyLevel.BEGINNER));
        assertThat(result).hasSize(1);
    }

    @Test
    void listWithThemeAndDifficultyFilter() {
        var mission = createMission(true, 0);
        when(missionRepository.findByThemeAndDifficulty(Theme.ASTRONOMY, DifficultyLevel.BEGINNER))
                .thenReturn(List.of(mission));

        var result = service.handle(new GetMissionsUseCase.ListAllQuery(Theme.ASTRONOMY, DifficultyLevel.BEGINNER));
        assertThat(result).hasSize(1);
    }

    @Test
    void listFiltersOutDisabledMissions() {
        var disabled = createMission(false, 0);
        when(missionRepository.findByEnabledTrue()).thenReturn(List.of(disabled));

        var result = service.handle(new GetMissionsUseCase.ListAllQuery());
        assertThat(result).isEmpty();
    }

    @Test
    void findByIdReturnsEnabledMission() {
        var mission = createMission(true, 0);
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));

        var result = service.handle(new GetMissionsUseCase.FindByIdQuery(missionId, userId));
        assertThat(result.getId()).isEqualTo(missionId);
    }

    @Test
    void findByIdThrowsWhenNotFound() {
        when(missionRepository.findById(missionId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.handle(new GetMissionsUseCase.FindByIdQuery(missionId)))
                .isInstanceOf(MissionNotFoundException.class);
    }

    @Test
    void findByIdThrowsWhenDisabled() {
        var mission = createMission(false, 0);
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        assertThatThrownBy(() -> service.handle(new GetMissionsUseCase.FindByIdQuery(missionId)))
                .isInstanceOf(MissionNotFoundException.class);
    }

    @Test
    void findByIdThrowsWhenLevelTooLow() {
        var mission = createMission(true, 10);
        var user = new User(userId, "u", "e@e", "h", 0, UserRole.USER, LocalDateTime.now());
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> service.handle(new GetMissionsUseCase.FindByIdQuery(missionId, userId)))
                .isInstanceOf(LevelRequiredException.class);
    }

    @Test
    void adminBypassesLevelCheckInFindById() {
        var mission = createMission(true, 10);
        var admin = new User(userId, "admin", "a@a", "h", 0, UserRole.ADMIN, LocalDateTime.now());
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(userRepository.findById(userId)).thenReturn(Optional.of(admin));
        var result = service.handle(new GetMissionsUseCase.FindByIdQuery(missionId, userId));
        assertThat(result.getId()).isEqualTo(missionId);
    }

    @Test
    void findByIdThrowsWhenMissionLocked() {
        var mission = Mission.builder()
                .id(missionId).title("M").briefing("B").objective("O")
                .ddlScript("DDL").techniques(List.of()).xpReward(10)
                .expectedResult(new ExpectedTuple(List.of(Map.of("x", 1))))
                .ordered(false).theme(Theme.ASTRONOMY)
                .difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(scenarioId).orderIndex(3)
                .enabled(true).requiredLevel(0)
                .build();
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        assertThatThrownBy(() -> service.handle(new GetMissionsUseCase.FindByIdQuery(missionId, userId)))
                .isInstanceOf(MissionLockedException.class);
    }
}
