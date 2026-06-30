package com.sqlab.application.usecase;

import com.sqlab.application.port.in.ManageMissionUseCase;
import com.sqlab.application.port.out.MissionCommandPort;
import com.sqlab.application.port.out.MissionQueryPort;
import com.sqlab.application.port.out.ScenarioMissionCascadePort;
import com.sqlab.application.port.out.TechniqueRepository;
import com.sqlab.application.port.out.ThemeRepository;
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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageMissionServiceTest {

    @Mock private MissionQueryPort missionQueryPort;
    @Mock private MissionCommandPort missionCommandPort;
    @Mock private ScenarioMissionCascadePort scenarioMissionCascadePort;
    @Mock private ThemeRepository themeRepository;
    @Mock private TechniqueRepository techniqueRepository;

    private ManageMissionService service;
    private final UUID missionId = UUID.randomUUID();
    private final Theme astronomyTheme = new Theme(UUID.randomUUID(), "ASTRONOMY", null, null);
    private final Technique selectTechnique = new Technique(null, "SELECT");
    private final Technique joinTechnique = new Technique(null, "JOIN");

    @BeforeEach
    void setUp() {
        service = new ManageMissionService(missionQueryPort, missionCommandPort, scenarioMissionCascadePort, themeRepository, techniqueRepository);
        lenient().when(themeRepository.findByName("ASTRONOMY")).thenReturn(Optional.of(astronomyTheme));
        lenient().when(techniqueRepository.findByNameIn(Set.of("SELECT"))).thenReturn(Set.of(selectTechnique));
        lenient().when(techniqueRepository.findByNameIn(Set.of("JOIN"))).thenReturn(Set.of(joinTechnique));
    }

    @Test
    void createMission() {
        when(missionCommandPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var cmd = new ManageMissionUseCase.CreateMissionCommand(
                "Title", "Briefing", "Objective", "Hint",
                "DDL", "DML", Set.of("SELECT"), 100, true,
                "ASTRONOMY", DifficultyLevel.BEGINNER,
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
        when(missionQueryPort.countByScenarioId(scenarioId)).thenReturn(3);
        when(missionCommandPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var cmd = new ManageMissionUseCase.CreateMissionCommand(
                "Title", "B", "O", null, "DDL", null,
                Set.of(), 50, false, "ASTRONOMY", DifficultyLevel.BEGINNER,
                List.of(Map.of("x", 1)), scenarioId, null, null);

        var result = service.create(cmd);
        assertThat(result.getOrderIndex()).isEqualTo(4);
        assertThat(result.getScenarioId()).isEqualTo(scenarioId);
    }

    @Test
    void updateMission() {
        var existing = Mission.builder().id(missionId).title("Old").briefing("B").objective("O")
                .ddlScript("DDL").techniques(Set.of()).xpReward(10)
                .expectedResult(new ExpectedTuple(List.of(Map.of("x", 1))))
                .ordered(false).theme(astronomyTheme).difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(null).orderIndex(null).enabled(true).build();
        when(missionQueryPort.findById(missionId)).thenReturn(Optional.of(existing));
        when(missionCommandPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var cmd = new ManageMissionUseCase.UpdateMissionCommand(
                missionId, "Updated", "B", "O", null, "DDL", null,
                Set.of("JOIN"), 200, true, "ASTRONOMY",
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
                Set.of(), 10, false, "ASTRONOMY",
                DifficultyLevel.BEGINNER, List.of(Map.of("x", 1)),
                null, null, null);
        when(missionQueryPort.findById(missionId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(cmd)).isInstanceOf(MissionNotFoundException.class);
    }

    @Test
    void deleteMission() {
        var mission = Mission.builder().id(missionId).title("T").briefing("B").objective("O")
                .ddlScript("DDL").techniques(Set.of()).xpReward(10)
                .expectedResult(new ExpectedTuple(List.of(Map.of("x", 1))))
                .ordered(false).theme(astronomyTheme).difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(null).orderIndex(null).enabled(true).build();
        when(missionQueryPort.findById(missionId)).thenReturn(Optional.of(mission));
        service.delete(missionId);
        verify(missionCommandPort).deleteById(missionId);
    }

    @Test
    void deleteThrowsWhenNotFound() {
        when(missionQueryPort.findById(missionId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(missionId)).isInstanceOf(MissionNotFoundException.class);
        verify(missionCommandPort, never()).deleteById(any());
    }

    @Test
    void validateScenarioConstraintThrows() {
        var cmd = new ManageMissionUseCase.UpdateMissionCommand(
                missionId, "T", "B", "O", null, "DDL", null,
                Set.of(), 10, false, "ASTRONOMY",
                DifficultyLevel.BEGINNER, List.of(Map.of("x", 1)),
                UUID.randomUUID(), null, null);
        assertThatThrownBy(() -> service.update(cmd))
                .isInstanceOf(IllegalArgumentException.class);
        verify(missionQueryPort, never()).findById(any());
    }
}
