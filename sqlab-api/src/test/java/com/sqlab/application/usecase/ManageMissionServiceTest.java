package com.sqlab.application.usecase;

import com.sqlab.application.port.in.ManageMissionUseCase;
import com.sqlab.application.port.in.ManageScenarioUseCase;
import com.sqlab.application.port.out.MissionRepository;
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
class ManageMissionServiceTest {

    @Mock private MissionRepository missionRepository;
    @Mock private ManageScenarioUseCase manageScenarioUseCase;

    private ManageMissionService service;
    private final UUID missionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new ManageMissionService(missionRepository, manageScenarioUseCase);
    }

    @Test
    void createMission() {
        when(missionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var cmd = new ManageMissionUseCase.CreateMissionCommand(
                "Title", "Briefing", "Objective", "Hint",
                "DDL", "DML", List.of("SELECT"), 100, true,
                Theme.ASTRONOMY, DifficultyLevel.BEGINNER,
                List.of(Map.of("x", 1)), null, null, null);

        var result = service.create(cmd);
        assertThat(result.getTitle()).isEqualTo("Title");
        assertThat(result.getXpReward()).isEqualTo(100);
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getId()).isNotNull();
    }

    @Test
    void createMissionWithScenarioAutoAssignsOrderIndex() {
        var scenarioId = UUID.randomUUID();
        when(missionRepository.countByScenarioId(scenarioId)).thenReturn(3);
        when(missionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var cmd = new ManageMissionUseCase.CreateMissionCommand(
                "Title", "B", "O", null, "DDL", null,
                List.of(), 50, false, Theme.ASTRONOMY, DifficultyLevel.BEGINNER,
                List.of(Map.of("x", 1)), scenarioId, null, null);

        var result = service.create(cmd);
        assertThat(result.getOrderIndex()).isEqualTo(4);
        assertThat(result.getScenarioId()).isEqualTo(scenarioId);
    }

    @Test
    void updateMission() {
        var existing = Mission.builder().id(missionId).title("Old").briefing("B").objective("O")
                .ddlScript("DDL").techniques(List.of()).xpReward(10)
                .expectedResult(new ExpectedTuple(List.of(Map.of("x", 1))))
                .ordered(false).theme(Theme.ASTRONOMY).difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(null).orderIndex(null).enabled(true).build();
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(existing));
        when(missionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var cmd = new ManageMissionUseCase.UpdateMissionCommand(
                missionId, "Updated", "B", "O", null, "DDL", null,
                List.of("JOIN"), 200, true, Theme.ASTRONOMY,
                DifficultyLevel.ADVANCED, List.of(Map.of("y", 2)),
                null, null, null);

        var result = service.update(cmd);
        assertThat(result.getTitle()).isEqualTo("Updated");
        assertThat(result.getXpReward()).isEqualTo(200);
    }

    @Test
    void updateThrowsWhenNotFound() {
        var cmd = new ManageMissionUseCase.UpdateMissionCommand(
                missionId, "T", "B", "O", null, "DDL", null,
                List.of(), 10, false, Theme.ASTRONOMY,
                DifficultyLevel.BEGINNER, List.of(Map.of("x", 1)),
                null, null, null);
        when(missionRepository.findById(missionId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(cmd)).isInstanceOf(MissionNotFoundException.class);
    }

    @Test
    void deleteMission() {
        var mission = Mission.builder().id(missionId).title("T").briefing("B").objective("O")
                .ddlScript("DDL").techniques(List.of()).xpReward(10)
                .expectedResult(new ExpectedTuple(List.of(Map.of("x", 1))))
                .ordered(false).theme(Theme.ASTRONOMY).difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(null).orderIndex(null).enabled(true).build();
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        service.delete(missionId);
        verify(missionRepository).deleteById(missionId);
    }

    @Test
    void deleteThrowsWhenNotFound() {
        when(missionRepository.findById(missionId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(missionId)).isInstanceOf(MissionNotFoundException.class);
        verify(missionRepository, never()).deleteById(any());
    }

    @Test
    void validateScenarioConstraintThrows() {
        var cmd = new ManageMissionUseCase.UpdateMissionCommand(
                missionId, "T", "B", "O", null, "DDL", null,
                List.of(), 10, false, Theme.ASTRONOMY,
                DifficultyLevel.BEGINNER, List.of(Map.of("x", 1)),
                UUID.randomUUID(), null, null);
        assertThatThrownBy(() -> service.update(cmd))
                .isInstanceOf(IllegalArgumentException.class);
        verify(missionRepository, never()).findById(any());
    }
}
