package com.sqlab.application.usecase;

import com.sqlab.application.port.in.ManageScenarioUseCase;
import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.domain.exception.ScenarioNotFoundException;
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
class ManageScenarioServiceTest {

    @Mock private ScenarioRepository scenarioRepository;
    @Mock private MissionRepository missionRepository;

    private ManageScenarioService service;
    private final UUID scenarioId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new ManageScenarioService(scenarioRepository, missionRepository);
    }

    @Test
    void createScenario() {
        when(scenarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var cmd = new ManageScenarioUseCase.CreateScenarioCommand("S1", "Desc", Theme.ASTRONOMY, true, 1);
        var result = service.create(cmd);
        assertThat(result.getTitle()).isEqualTo("S1");
        assertThat(result.getTheme()).isEqualTo(Theme.ASTRONOMY);
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    void updateScenario() {
        var existing = new Scenario(scenarioId, "Old", "Desc", Theme.ASTRONOMY, true, 1);
        when(scenarioRepository.findById(scenarioId)).thenReturn(Optional.of(existing));
        when(scenarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var cmd = new ManageScenarioUseCase.UpdateScenarioCommand(scenarioId, "New", "NewDesc", Theme.CYBERSECURITY, false, 2);
        var result = service.update(cmd);
        assertThat(result.getTitle()).isEqualTo("New");
        assertThat(result.getTheme()).isEqualTo(Theme.CYBERSECURITY);
        assertThat(result.isEnabled()).isFalse();
        verify(missionRepository).setEnabledByScenarioId(scenarioId, false);
    }

    @Test
    void updateThrowsWhenNotFound() {
        var cmd = new ManageScenarioUseCase.UpdateScenarioCommand(scenarioId, "T", "D", Theme.ASTRONOMY, true, 1);
        when(scenarioRepository.findById(scenarioId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(cmd)).isInstanceOf(ScenarioNotFoundException.class);
    }

    @Test
    void deleteScenario() {
        var existing = new Scenario(scenarioId, "T", "D", Theme.ASTRONOMY, true, 1);
        when(scenarioRepository.findById(scenarioId)).thenReturn(Optional.of(existing));
        service.delete(scenarioId);
        verify(scenarioRepository).deleteById(scenarioId);
    }

    @Test
    void deleteThrowsWhenNotFound() {
        when(scenarioRepository.findById(scenarioId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(scenarioId)).isInstanceOf(ScenarioNotFoundException.class);
        verify(scenarioRepository, never()).deleteById(any());
    }

    @Test
    void reorderMissions() {
        var m1 = Mission.builder().id(UUID.randomUUID()).title("M1").briefing("B").objective("O")
                .ddlScript("DDL").techniques(List.of()).xpReward(10)
                .expectedResult(new ExpectedTuple(List.of(Map.of("x", 1))))
                .ordered(false).theme(Theme.ASTRONOMY).difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(scenarioId).orderIndex(1).enabled(true).build();
        var m2 = Mission.builder().id(UUID.randomUUID()).title("M2").briefing("B").objective("O")
                .ddlScript("DDL").techniques(List.of()).xpReward(10)
                .expectedResult(new ExpectedTuple(List.of(Map.of("x", 1))))
                .ordered(false).theme(Theme.ASTRONOMY).difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(scenarioId).orderIndex(2).enabled(true).build();

        when(scenarioRepository.findById(scenarioId)).thenReturn(Optional.of(
                new Scenario(scenarioId, "S", "D", Theme.ASTRONOMY, true, 0)));
        when(missionRepository.findByScenarioIdOrderByOrderIndex(scenarioId)).thenReturn(List.of(m1, m2));
        when(missionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var cmd = new ManageScenarioUseCase.ReorderMissionsCommand(scenarioId, List.of(m2.getId(), m1.getId()));
        service.reorderMissions(cmd);

        verify(missionRepository, times(4)).save(any());
    }

    @Test
    void reorderThrowsWhenScenarioNotFound() {
        when(scenarioRepository.findById(scenarioId)).thenReturn(Optional.empty());
        var cmd = new ManageScenarioUseCase.ReorderMissionsCommand(scenarioId, List.of());
        assertThatThrownBy(() -> service.reorderMissions(cmd)).isInstanceOf(ScenarioNotFoundException.class);
    }

    @Test
    void reorderThrowsWhenCountMismatch() {
        var m1 = Mission.builder().id(UUID.randomUUID()).title("M1").briefing("B").objective("O")
                .ddlScript("DDL").techniques(List.of()).xpReward(10)
                .expectedResult(new ExpectedTuple(List.of(Map.of("x", 1))))
                .ordered(false).theme(Theme.ASTRONOMY).difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(scenarioId).orderIndex(1).enabled(true).build();
        when(scenarioRepository.findById(scenarioId)).thenReturn(Optional.of(
                new Scenario(scenarioId, "S", "D", Theme.ASTRONOMY, true, 0)));
        when(missionRepository.findByScenarioIdOrderByOrderIndex(scenarioId)).thenReturn(List.of(m1));
        var cmd = new ManageScenarioUseCase.ReorderMissionsCommand(scenarioId, List.of());
        assertThatThrownBy(() -> service.reorderMissions(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("count mismatch");
    }
}
