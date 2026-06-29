package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetMissionsUseCase;
import com.sqlab.application.port.out.MissionQueryPort;
import com.sqlab.application.port.out.MissionValidationPort;
import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.application.port.out.ThemeRepository;
import com.sqlab.domain.exception.LevelRequiredException;
import com.sqlab.domain.exception.MissionLockedException;
import com.sqlab.domain.exception.MissionNotFoundException;
import com.sqlab.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetMissionsServiceTest {

    @Mock private MissionQueryPort missionQueryPort;
    @Mock private MissionValidationPort missionValidationPort;
    @Mock private ScenarioRepository scenarioRepository;
    @Mock private MissionAccessValidator missionAccessValidator;
    @Mock private ThemeRepository themeRepository;

    @Captor private ArgumentCaptor<String> nameCaptor;
    @Captor private ArgumentCaptor<Theme> themeCaptor;
    @Captor private ArgumentCaptor<DifficultyLevel> difficultyCaptor;
    @Captor private ArgumentCaptor<String> scenarioScopeCaptor;
    @Captor private ArgumentCaptor<Integer> pageCaptor;
    @Captor private ArgumentCaptor<Integer> sizeCaptor;

    private GetMissionsService service;
    private final UUID userId = UUID.randomUUID();
    private final UUID missionId = UUID.randomUUID();
    private final UUID scenarioId = UUID.randomUUID();
    private final Theme astronomyTheme = new Theme(UUID.randomUUID(), "ASTRONOMY", null, null);

    @BeforeEach
    void setUp() {
        service = new GetMissionsService(missionQueryPort, missionValidationPort, scenarioRepository, missionAccessValidator, themeRepository);
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

    private Page<Mission> pageOf(Mission... missions) {
        List<Mission> list = Arrays.asList(missions);
        return new Page<>(list, list.size(), 1, 0, 12);
    }

    @Test
    void listAllWithoutFilters() {
        var mission = createMission(true, 0);
        when(missionQueryPort.findByFilters(isNull(), isNull(), isNull(), isNull(), eq(0), eq(12)))
                .thenReturn(pageOf(mission));

        Page<Mission> result = service.handle(new GetMissionsUseCase.ListAllQuery());

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void listWithThemeFilter() {
        var mission = createMission(true, 0);
        when(missionQueryPort.findByFilters(isNull(), eq(astronomyTheme), isNull(), isNull(), eq(0), eq(12)))
                .thenReturn(pageOf(mission));

        Page<Mission> result = service.handle(new GetMissionsUseCase.ListAllQuery("ASTRONOMY", null));

        assertThat(result.content()).hasSize(1);
    }

    @Test
    void listWithDifficultyFilter() {
        var mission = createMission(true, 0);
        when(missionQueryPort.findByFilters(isNull(), isNull(), eq(DifficultyLevel.BEGINNER), isNull(), eq(0), eq(12)))
                .thenReturn(pageOf(mission));

        Page<Mission> result = service.handle(new GetMissionsUseCase.ListAllQuery(null, DifficultyLevel.BEGINNER));

        assertThat(result.content()).hasSize(1);
    }

    @Test
    void listWithThemeAndDifficultyFilter() {
        var mission = createMission(true, 0);
        when(missionQueryPort.findByFilters(isNull(), eq(astronomyTheme), eq(DifficultyLevel.BEGINNER), isNull(), eq(0), eq(12)))
                .thenReturn(pageOf(mission));

        Page<Mission> result = service.handle(new GetMissionsUseCase.ListAllQuery("ASTRONOMY", DifficultyLevel.BEGINNER));

        assertThat(result.content()).hasSize(1);
    }

    @Test
    void listAllPaginatedWithSearchName() {
        var mission = createMission(true, 0);
        when(missionQueryPort.findByFilters(eq("join"), isNull(), isNull(), isNull(), eq(0), eq(12)))
                .thenReturn(pageOf(mission));

        Page<Mission> result = service.handle(
                new GetMissionsUseCase.ListAllQuery(null, null, "join", null, 0, 12));

        assertThat(result.content()).hasSize(1);
    }

    @Test
    void listAllPaginatedWithScenarioFilter() {
        var mission = createMission(true, 0);
        when(missionQueryPort.findByFilters(isNull(), isNull(), isNull(), eq("IN_SCENARIO"), eq(0), eq(12)))
                .thenReturn(pageOf(mission));

        Page<Mission> result = service.handle(
                new GetMissionsUseCase.ListAllQuery(null, null, null, "IN_SCENARIO", 0, 12));

        assertThat(result.content()).hasSize(1);
    }

    @Test
    void listAllPaginatedRespectsPageSize() {
        var mission = createMission(true, 0);
        Page<Mission> expectedPage = new Page<>(List.of(mission), 1, 1, 0, 12);
        when(missionQueryPort.findByFilters(isNull(), isNull(), isNull(), isNull(), eq(0), eq(12)))
                .thenReturn(expectedPage);

        Page<Mission> result = service.handle(new GetMissionsUseCase.ListAllQuery());

        assertThat(result.number()).isZero();
        assertThat(result.size()).isEqualTo(12);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    void listAllPaginatedSecondPage() {
        var mission = createMission(true, 0);
        Page<Mission> expectedPage = new Page<>(List.of(mission), 13, 2, 1, 12);
        when(missionQueryPort.findByFilters(isNull(), isNull(), isNull(), isNull(), eq(1), eq(12)))
                .thenReturn(expectedPage);

        Page<Mission> result = service.handle(
                new GetMissionsUseCase.ListAllQuery(null, null, null, null, 1, 12));

        assertThat(result.number()).isEqualTo(1);
        assertThat(result.totalElements()).isEqualTo(13);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    void listAllThrowsWhenThemeNotFound() {
        when(themeRepository.findByName("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.handle(
                new GetMissionsUseCase.ListAllQuery("UNKNOWN", null)))
                .isInstanceOf(com.sqlab.domain.exception.ThemeNotFoundException.class);
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
