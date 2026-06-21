package com.sqlab.application.usecase;

import com.sqlab.application.port.out.MissionQueryPort;
import com.sqlab.application.port.out.MissionValidationPort;
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
class MissionAccessValidatorTest {

    @Mock private MissionQueryPort missionQueryPort;
    @Mock private MissionValidationPort missionValidationPort;
    @Mock private UserRepository userRepository;
    @Mock private ScenarioRepository scenarioRepository;

    private MissionAccessValidator validator;
    private final UUID missionId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID scenarioId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        validator = new MissionAccessValidator(missionQueryPort, missionValidationPort, userRepository, scenarioRepository);
    }

    private Mission createMission(boolean enabled, UUID scenario, Integer orderIndex, int requiredLevel) {
        return Mission.builder()
                .id(missionId).title("M").briefing("B").objective("O")
                .ddlScript("DDL").techniques(List.of()).xpReward(10)
                .expectedResult(new ExpectedTuple(List.of(Map.of("x", 1))))
                .ordered(false).theme(new Theme(UUID.randomUUID(), "ASTRONOMY", null, null))
                .difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(scenario).orderIndex(orderIndex)
                .enabled(enabled).requiredLevel(requiredLevel)
                .build();
    }

    @Test
    void ensureAccessible_returnsMissionWhenEnabled() {
        var mission = createMission(true, null, null, 0);
        when(missionQueryPort.findById(missionId)).thenReturn(Optional.of(mission));

        var result = validator.ensureAccessible(missionId, userId);
        assertThat(result).isEqualTo(mission);
    }

    @Test
    void ensureAccessible_throwsWhenMissionNotFound() {
        when(missionQueryPort.findById(missionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.ensureAccessible(missionId, userId))
                .isInstanceOf(MissionNotFoundException.class);
    }

    @Test
    void ensureAccessible_throwsWhenMissionDisabled() {
        var mission = createMission(false, null, null, 0);
        when(missionQueryPort.findById(missionId)).thenReturn(Optional.of(mission));

        assertThatThrownBy(() -> validator.ensureAccessible(missionId, userId))
                .isInstanceOf(MissionNotFoundException.class);
    }

    @Test
    void ensureAccessible_throwsWhenScenarioHasDisabledMissions() {
        var mission = createMission(true, scenarioId, 1, 0);
        when(missionQueryPort.findById(missionId)).thenReturn(Optional.of(mission));
        when(missionValidationPort.existsByScenarioIdAndEnabledFalse(scenarioId)).thenReturn(true);

        assertThatThrownBy(() -> validator.ensureAccessible(missionId, userId))
                .isInstanceOf(MissionNotFoundException.class);
    }

    @Test
    void checkLevel_passesWhenLevelSufficient() {
        var mission = createMission(true, null, null, 5);
        var user = new User(userId, "u", "e@e.com", "pwd", 5000, UserRole.USER, null);
        when(missionQueryPort.findById(missionId)).thenReturn(Optional.of(mission));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var result = validator.checkLevel(missionId, userId);
        assertThat(result).isEqualTo(mission);
    }

    @Test
    void checkLevel_throwsWhenLevelInsufficient() {
        var mission = createMission(true, null, null, 10);
        var user = new User(userId, "u", "e@e.com", "pwd", 0, UserRole.USER, null);
        when(missionQueryPort.findById(missionId)).thenReturn(Optional.of(mission));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> validator.checkLevel(missionId, userId))
                .isInstanceOf(LevelRequiredException.class);
    }

    @Test
    void checkLevel_adminBypassesLevelCheck() {
        var mission = createMission(true, null, null, 10);
        var admin = new User(userId, "a", "a@a.com", "pwd", 0, UserRole.ADMIN, null);
        when(missionQueryPort.findById(missionId)).thenReturn(Optional.of(mission));
        when(userRepository.findById(userId)).thenReturn(Optional.of(admin));

        var result = validator.checkLevel(missionId, userId);
        assertThat(result).isEqualTo(mission);
    }

    @Test
    void checkLevel_passesWhenRequiredLevelIsZero() {
        var mission = createMission(true, null, null, 0);
        when(missionQueryPort.findById(missionId)).thenReturn(Optional.of(mission));

        var result = validator.checkLevel(missionId, userId);
        assertThat(result).isEqualTo(mission);
        verifyNoInteractions(userRepository);
    }

    @Test
    void checkLevel_passesWhenUserIdIsNull() {
        var mission = createMission(true, null, null, 5);
        when(missionQueryPort.findById(missionId)).thenReturn(Optional.of(mission));

        var result = validator.checkLevel(missionId, null);
        assertThat(result).isEqualTo(mission);
        verifyNoInteractions(userRepository);
    }

    @Test
    void checkOrder_passesWhenOrderIndexIsOne() {
        var mission = createMission(true, scenarioId, 1, 0);
        when(missionQueryPort.findById(missionId)).thenReturn(Optional.of(mission));

        var result = validator.ensureAccessible(missionId, userId);
        assertThat(result).isEqualTo(mission);
        verify(missionValidationPort, never()).isPreviousMissionCompleted(any(), any(), anyInt());
    }

    @Test
    void checkOrder_passesWhenPreviousMissionCompleted() {
        var mission = createMission(true, scenarioId, 3, 0);
        when(missionQueryPort.findById(missionId)).thenReturn(Optional.of(mission));
        when(missionValidationPort.isPreviousMissionCompleted(userId, scenarioId, 2)).thenReturn(true);

        var result = validator.ensureAccessible(missionId, userId);
        assertThat(result).isEqualTo(mission);
    }

    @Test
    void checkOrder_throwsWhenPreviousMissionNotCompleted() {
        var mission = createMission(true, scenarioId, 2, 0);
        when(missionQueryPort.findById(missionId)).thenReturn(Optional.of(mission));
        when(missionValidationPort.isPreviousMissionCompleted(userId, scenarioId, 1)).thenReturn(false);
        when(scenarioRepository.findById(scenarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.ensureAccessible(missionId, userId))
                .isInstanceOf(MissionLockedException.class);
    }

    @Test
    void checkOrder_doesNotCheckWhenUserIdIsNull() {
        var mission = createMission(true, scenarioId, 2, 0);
        when(missionQueryPort.findById(missionId)).thenReturn(Optional.of(mission));

        var result = validator.ensureAccessible(missionId, null);
        assertThat(result).isEqualTo(mission);
        verify(missionValidationPort, never()).isPreviousMissionCompleted(any(), any(), anyInt());
    }

    @Test
    void checkOrder_doesNotCheckWhenScenarioIdIsNull() {
        var mission = createMission(true, null, 2, 0);
        when(missionQueryPort.findById(missionId)).thenReturn(Optional.of(mission));

        var result = validator.ensureAccessible(missionId, userId);
        assertThat(result).isEqualTo(mission);
        verify(missionValidationPort, never()).isPreviousMissionCompleted(any(), any(), anyInt());
    }

    @Test
    void ensureAccessible_throwsWhenLevelInsufficient() {
        var mission = createMission(true, null, null, 5);
        var user = new User(userId, "u", "e@e.com", "pwd", 0, UserRole.USER, null);
        when(missionQueryPort.findById(missionId)).thenReturn(Optional.of(mission));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> validator.ensureAccessible(missionId, userId))
                .isInstanceOf(LevelRequiredException.class);
    }

    @Test
    void ensureAccessible_throwsWhenMissionLocked() {
        var mission = createMission(true, scenarioId, 2, 0);
        when(missionQueryPort.findById(missionId)).thenReturn(Optional.of(mission));
        when(missionValidationPort.isPreviousMissionCompleted(userId, scenarioId, 1)).thenReturn(false);
        when(scenarioRepository.findById(scenarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.ensureAccessible(missionId, userId))
                .isInstanceOf(MissionLockedException.class);
    }
}
