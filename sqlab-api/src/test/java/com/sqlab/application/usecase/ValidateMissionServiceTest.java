package com.sqlab.application.usecase;

import com.sqlab.application.port.in.ValidateMissionUseCase;
import com.sqlab.application.port.out.ProgressRepository;
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidateMissionServiceTest {

    @Mock private ProgressRepository progressRepository;
    @Mock private UserRepository userRepository;
    @Mock private MissionAccessValidator missionAccessValidator;

    private ValidateMissionService service;
    private final UUID userId = UUID.randomUUID();
    private final UUID missionId = UUID.randomUUID();
    private final UUID scenarioId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new ValidateMissionService(progressRepository, userRepository, missionAccessValidator);
    }

    private Mission createMission(int requiredLevel, boolean enabled, UUID scenarioId, Integer orderIndex) {
        return Mission.builder()
                .id(missionId)
                .title("Test")
                .briefing("B")
                .objective("O")
                .ddlScript("DDL")
                .techniques(Set.of())
                .xpReward(100)
                .expectedResult(new ExpectedTuple(List.of(Map.of("v", 42))))
                .ordered(true)
                .theme(new Theme(UUID.randomUUID(), "ASTRONOMY", null, null))
                .difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(scenarioId)
                .orderIndex(orderIndex)
                .enabled(enabled)
                .requiredLevel(requiredLevel)
                .build();
    }

    @Test
    void throwsWhenMissionNotFound() {
        when(missionAccessValidator.ensureAccessible(missionId, userId))
                .thenThrow(new MissionNotFoundException(missionId));
        assertThatThrownBy(() -> service.handle(new ValidateMissionUseCase.Command(userId, missionId, List.of())))
                .isInstanceOf(MissionNotFoundException.class);
    }

    @Test
    void throwsWhenMissionDisabled() {
        when(missionAccessValidator.ensureAccessible(missionId, userId))
                .thenThrow(new MissionNotFoundException(missionId));
        assertThatThrownBy(() -> service.handle(new ValidateMissionUseCase.Command(userId, missionId, List.of())))
                .isInstanceOf(MissionNotFoundException.class);
    }

    @Test
    void throwsWhenUserNotFound() {
        when(missionAccessValidator.ensureAccessible(missionId, userId))
                .thenThrow(new MissionNotFoundException(missionId));
        assertThatThrownBy(() -> service.handle(new ValidateMissionUseCase.Command(userId, missionId, List.of())))
                .isInstanceOf(MissionNotFoundException.class);
    }

    @Test
    void throwsWhenLevelTooLow() {
        var mission = createMission(5, true, null, null);
        when(missionAccessValidator.ensureAccessible(missionId, userId))
                .thenThrow(new LevelRequiredException(5, 0));
        assertThatThrownBy(() -> service.handle(new ValidateMissionUseCase.Command(userId, missionId, List.of())))
                .isInstanceOf(LevelRequiredException.class);
    }

    @Test
    void throwsWhenMissionLocked() {
        var mission = createMission(0, true, scenarioId, 2);
        when(missionAccessValidator.ensureAccessible(missionId, userId))
                .thenThrow(new MissionLockedException(missionId, scenarioId, "S1"));
        assertThatThrownBy(() -> service.handle(new ValidateMissionUseCase.Command(userId, missionId, List.of())))
                .isInstanceOf(MissionLockedException.class);
    }

    @Test
    void doesNotLockFirstMission() {
        var mission = createMission(0, true, scenarioId, 1);
        when(missionAccessValidator.ensureAccessible(missionId, userId)).thenReturn(mission);
        var result = service.handle(new ValidateMissionUseCase.Command(userId, missionId, List.of(Map.of("v", 42))));
        assertThat(result.correct()).isTrue();
    }

    @Test
    void correctValidationSavesProgressAndAwardsXp() {
        var mission = createMission(0, true, scenarioId, 1);
        when(missionAccessValidator.ensureAccessible(missionId, userId)).thenReturn(mission);
        when(progressRepository.existsByUserIdAndMissionId(userId, missionId)).thenReturn(false);

        var result = service.handle(new ValidateMissionUseCase.Command(userId, missionId, List.of(Map.of("v", 42))));

        assertThat(result.correct()).isTrue();
        verify(progressRepository).save(any());
        verify(userRepository).addXp(userId, 100);
    }

    @Test
    void noXpFarmingWhenAlreadyCompleted() {
        var mission = createMission(0, true, null, null);
        when(missionAccessValidator.ensureAccessible(missionId, userId)).thenReturn(mission);
        when(progressRepository.existsByUserIdAndMissionId(userId, missionId)).thenReturn(true);

        var result = service.handle(new ValidateMissionUseCase.Command(userId, missionId, List.of(Map.of("v", 42))));

        assertThat(result.correct()).isTrue();
        verify(progressRepository, never()).save(any());
        verify(userRepository, never()).addXp(any(), anyInt());
    }

    @Test
    void incorrectValidationDoesNotSaveProgress() {
        var mission = createMission(0, true, null, null);
        when(missionAccessValidator.ensureAccessible(missionId, userId)).thenReturn(mission);

        var result = service.handle(new ValidateMissionUseCase.Command(userId, missionId, List.of(Map.of("v", 99))));

        assertThat(result.correct()).isFalse();
        verify(progressRepository, never()).save(any());
        verify(userRepository, never()).addXp(any(), anyInt());
    }

    @Test
    void adminBypassesLevelCheck() {
        var mission = createMission(10, true, null, null);
        when(missionAccessValidator.ensureAccessible(missionId, userId)).thenReturn(mission);

        var result = service.handle(new ValidateMissionUseCase.Command(userId, missionId, List.of(Map.of("v", 42))));

        assertThat(result.correct()).isTrue();
    }
}
