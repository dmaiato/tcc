package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetMissionsUseCase;
import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.application.port.out.ThemeRepository;
import com.sqlab.domain.exception.LevelRequiredException;
import com.sqlab.domain.exception.MissionLockedException;
import com.sqlab.domain.exception.MissionNotFoundException;
import com.sqlab.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetMissionsServiceTest {

    @Mock private MissionRepository missionRepository;
    @Mock private ScenarioRepository scenarioRepository;
    @Mock private MissionAccessValidator missionAccessValidator;
    @Mock private ThemeRepository themeRepository;

    private GetMissionsService service;
    private final UUID userId = UUID.randomUUID();
    private final UUID missionId = UUID.randomUUID();
    private final UUID scenarioId = UUID.randomUUID();
    private final Theme astronomyTheme = new Theme(UUID.randomUUID(), "ASTRONOMY", null, null);

    @BeforeEach
    void setUp() {
        service = new GetMissionsService(missionRepository, scenarioRepository, missionAccessValidator, themeRepository);
        lenient().when(themeRepository.findByName("ASTRONOMY")).thenReturn(Optional.of(astronomyTheme));
    }

    private Mission createMission(boolean enabled, int requiredLevel) {
        return Mission.builder()
                .id(missionId).title("M").briefing("B").objective("O")
                .ddlScript("DDL").techniques(List.of()).xpReward(10)
                .expectedResult(new ExpectedTuple(List.of(Map.of("x", 1))))
                .ordered(false).theme(new Theme(UUID.randomUUID(), "ASTRONOMY", null, null))
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
        when(missionRepository.findByTheme(astronomyTheme)).thenReturn(List.of(mission));

        var result = service.handle(new GetMissionsUseCase.ListAllQuery("ASTRONOMY", null));
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
        when(missionRepository.findByThemeAndDifficulty(astronomyTheme, DifficultyLevel.BEGINNER))
                .thenReturn(List.of(mission));

        var result = service.handle(new GetMissionsUseCase.ListAllQuery("ASTRONOMY", DifficultyLevel.BEGINNER));
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
        when(missionAccessValidator.ensureAccessible(missionId, userId)).thenReturn(mission);

        var result = service.handle(new GetMissionsUseCase.FindByIdQuery(missionId, userId));
        assertThat(result.getId()).isEqualTo(missionId);
    }

    @Test
    void findByIdThrowsWhenNotFound() {
        when(missionAccessValidator.ensureAccessible(missionId, null)).thenThrow(new MissionNotFoundException(missionId));
        assertThatThrownBy(() -> service.handle(new GetMissionsUseCase.FindByIdQuery(missionId)))
                .isInstanceOf(MissionNotFoundException.class);
    }

    @Test
    void findByIdThrowsWhenDisabled() {
        when(missionAccessValidator.ensureAccessible(missionId, null)).thenThrow(new MissionNotFoundException(missionId));
        assertThatThrownBy(() -> service.handle(new GetMissionsUseCase.FindByIdQuery(missionId)))
                .isInstanceOf(MissionNotFoundException.class);
    }

    @Test
    void findByIdThrowsWhenLevelTooLow() {
        var mission = createMission(true, 10);
        when(missionAccessValidator.ensureAccessible(missionId, userId)).thenThrow(new LevelRequiredException(10, 0));
        assertThatThrownBy(() -> service.handle(new GetMissionsUseCase.FindByIdQuery(missionId, userId)))
                .isInstanceOf(LevelRequiredException.class);
    }

    @Test
    void adminBypassesLevelCheckInFindById() {
        var mission = createMission(true, 10);
        when(missionAccessValidator.ensureAccessible(missionId, userId)).thenReturn(mission);
        var result = service.handle(new GetMissionsUseCase.FindByIdQuery(missionId, userId));
        assertThat(result.getId()).isEqualTo(missionId);
    }

    @Test
    void findByIdThrowsWhenMissionLocked() {
        when(missionAccessValidator.ensureAccessible(missionId, userId)).thenThrow(new MissionLockedException(missionId, scenarioId, "S1"));
        assertThatThrownBy(() -> service.handle(new GetMissionsUseCase.FindByIdQuery(missionId, userId)))
                .isInstanceOf(MissionLockedException.class);
    }
}
