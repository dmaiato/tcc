package com.sqlab.application.usecase;

import com.sqlab.application.port.in.ValidateMissionUseCase;
import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.application.port.out.ProgressRepository;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidateMissionServiceTest {

    @Mock private MissionRepository missionRepository;
    @Mock private ProgressRepository progressRepository;
    @Mock private UserRepository userRepository;
    @Mock private ScenarioRepository scenarioRepository;

    private ValidateMissionService service;
    private final UUID userId = UUID.randomUUID();
    private final UUID missionId = UUID.randomUUID();
    private final UUID scenarioId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new ValidateMissionService(missionRepository, progressRepository, userRepository, scenarioRepository);
    }

    private Mission createMission(int requiredLevel, boolean enabled, UUID scenarioId, Integer orderIndex) {
        return Mission.builder()
                .id(missionId)
                .title("Test")
                .briefing("B")
                .objective("O")
                .ddlScript("DDL")
                .techniques(List.of())
                .xpReward(100)
                .expectedResult(new ExpectedTuple(List.of(Map.of("v", 42))))
                .ordered(true)
                .theme(Theme.ASTRONOMY)
                .difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(scenarioId)
                .orderIndex(orderIndex)
                .enabled(enabled)
                .requiredLevel(requiredLevel)
                .build();
    }

    @Test
    void throwsWhenMissionNotFound() {
        when(missionRepository.findById(missionId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.handle(new ValidateMissionUseCase.Command(userId, missionId, List.of())))
                .isInstanceOf(MissionNotFoundException.class);
    }

    @Test
    void throwsWhenMissionDisabled() {
        var mission = createMission(0, false, null, null);
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        assertThatThrownBy(() -> service.handle(new ValidateMissionUseCase.Command(userId, missionId, List.of())))
                .isInstanceOf(MissionNotFoundException.class);
    }

    @Test
    void throwsWhenUserNotFound() {
        var mission = createMission(0, true, null, null);
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.handle(new ValidateMissionUseCase.Command(userId, missionId, List.of())))
                .isInstanceOf(MissionNotFoundException.class);
    }

    @Test
    void throwsWhenLevelTooLow() {
        var mission = createMission(5, true, null, null);
        var user = new User(userId, "user", "e@e", "hash", 0, UserRole.USER, java.time.LocalDateTime.now());
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> service.handle(new ValidateMissionUseCase.Command(userId, missionId, List.of())))
                .isInstanceOf(LevelRequiredException.class);
    }

    @Test
    void throwsWhenMissionLocked() {
        var mission = createMission(0, true, scenarioId, 2);
        var user = new User(userId, "user", "e@e", "hash", 10000, UserRole.USER, java.time.LocalDateTime.now());
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(missionRepository.isPreviousMissionCompleted(userId, scenarioId, 1)).thenReturn(false);
        when(scenarioRepository.findById(scenarioId)).thenReturn(Optional.of(new Scenario(scenarioId, "S1", "", Theme.ASTRONOMY, true, 0)));
        assertThatThrownBy(() -> service.handle(new ValidateMissionUseCase.Command(userId, missionId, List.of())))
                .isInstanceOf(MissionLockedException.class);
    }

    @Test
    void doesNotLockFirstMission() {
        var mission = createMission(0, true, scenarioId, 1);
        var user = new User(userId, "user", "e@e", "hash", 10000, UserRole.USER, java.time.LocalDateTime.now());
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        var result = service.handle(new ValidateMissionUseCase.Command(userId, missionId, List.of(Map.of("v", 42))));
        assertThat(result.correct()).isTrue();
        verify(missionRepository, never()).isPreviousMissionCompleted(any(), any(), anyInt());
    }

    @Test
    void correctValidationSavesProgressAndAwardsXp() {
        var mission = createMission(0, true, scenarioId, 1);
        var user = new User(userId, "user", "e@e", "hash", 0, UserRole.USER, java.time.LocalDateTime.now());
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(progressRepository.existsByUserIdAndMissionId(userId, missionId)).thenReturn(false);

        var result = service.handle(new ValidateMissionUseCase.Command(userId, missionId, List.of(Map.of("v", 42))));

        assertThat(result.correct()).isTrue();
        verify(progressRepository).save(any());
        verify(userRepository).save(user);
        assertThat(user.getXp()).isEqualTo(100);
    }

    @Test
    void noXpFarmingWhenAlreadyCompleted() {
        var mission = createMission(0, true, null, null);
        var user = new User(userId, "user", "e@e", "hash", 0, UserRole.USER, java.time.LocalDateTime.now());
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(progressRepository.existsByUserIdAndMissionId(userId, missionId)).thenReturn(true);

        var result = service.handle(new ValidateMissionUseCase.Command(userId, missionId, List.of(Map.of("v", 42))));

        assertThat(result.correct()).isTrue();
        verify(progressRepository, never()).save(any());
        verify(userRepository, never()).save(any());
        assertThat(user.getXp()).isZero();
    }

    @Test
    void incorrectValidationDoesNotSaveProgress() {
        var mission = createMission(0, true, null, null);
        var user = new User(userId, "user", "e@e", "hash", 0, UserRole.USER, java.time.LocalDateTime.now());
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var result = service.handle(new ValidateMissionUseCase.Command(userId, missionId, List.of(Map.of("v", 99))));

        assertThat(result.correct()).isFalse();
        verify(progressRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void adminBypassesLevelCheck() {
        var mission = createMission(10, true, null, null);
        var admin = new User(userId, "admin", "a@a", "hash", 0, UserRole.ADMIN, java.time.LocalDateTime.now());
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(userRepository.findById(userId)).thenReturn(Optional.of(admin));

        var result = service.handle(new ValidateMissionUseCase.Command(userId, missionId, List.of(Map.of("v", 42))));

        assertThat(result.correct()).isTrue();
    }
}
