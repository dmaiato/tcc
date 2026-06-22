package com.sqlab.application.usecase;

import com.sqlab.application.port.in.ManageScenarioUseCase;
import com.sqlab.application.port.out.MissionCommandPort;
import com.sqlab.application.port.out.MissionQueryPort;
import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.application.port.out.ThemeRepository;
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
    @Mock private MissionQueryPort missionQueryPort;
    @Mock private MissionCommandPort missionCommandPort;
    @Mock private ThemeRepository themeRepository;

    private ManageScenarioService service;
    private final UUID scenarioId = UUID.randomUUID();
    private final Theme astronomyTheme = new Theme(UUID.randomUUID(), "ASTRONOMY", null, null);
    private final Theme cybersecurityTheme = new Theme(UUID.randomUUID(), "CYBERSECURITY", null, null);

    @BeforeEach
    void setUp() {
        service = new ManageScenarioService(scenarioRepository, missionQueryPort, missionCommandPort, themeRepository);
        lenient().when(themeRepository.findByName("ASTRONOMY")).thenReturn(Optional.of(astronomyTheme));
        lenient().when(themeRepository.findByName("CYBERSECURITY")).thenReturn(Optional.of(cybersecurityTheme));
    }

    @Test
    void createScenario() {
        when(scenarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var cmd = new ManageScenarioUseCase.CreateScenarioCommand("S1", "Desc", "ASTRONOMY", true, 1);
        var result = service.create(cmd);
        assertThat(result.getTitle()).isEqualTo("S1");
        assertThat(result.getTheme()).isEqualTo(astronomyTheme);
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    void updateScenario() {
        var existing = new Scenario(scenarioId, "Old", "Desc", astronomyTheme, 1, true);
        when(scenarioRepository.findById(scenarioId)).thenReturn(Optional.of(existing));
        when(scenarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var cmd = new ManageScenarioUseCase.UpdateScenarioCommand(scenarioId, "New", "NewDesc", "CYBERSECURITY", false, 2);
        var result = service.update(cmd);
        assertThat(result.getTitle()).isEqualTo("New");
        assertThat(result.getTheme()).isEqualTo(cybersecurityTheme);
        assertThat(result.isEnabled()).isFalse();
        verify(missionCommandPort).setEnabledByScenarioId(scenarioId, false);
    }

    @Test
    void updateThrowsWhenNotFound() {
        var cmd = new ManageScenarioUseCase.UpdateScenarioCommand(scenarioId, "T", "D", "ASTRONOMY", true, 1);
        when(scenarioRepository.findById(scenarioId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(cmd)).isInstanceOf(ScenarioNotFoundException.class);
    }

    @Test
    void deleteScenario() {
        var existing = new Scenario(scenarioId, "T", "D", astronomyTheme, 1, true);
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
                .ordered(false).theme(astronomyTheme).difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(scenarioId).orderIndex(1).enabled(true).build();
        var m2 = Mission.builder().id(UUID.randomUUID()).title("M2").briefing("B").objective("O")
                .ddlScript("DDL").techniques(List.of()).xpReward(10)
                .expectedResult(new ExpectedTuple(List.of(Map.of("x", 1))))
                .ordered(false).theme(astronomyTheme).difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(scenarioId).orderIndex(2).enabled(true).build();

        when(scenarioRepository.findById(scenarioId)).thenReturn(Optional.of(
                new Scenario(scenarioId, "S", "D", astronomyTheme, 0, true)));
        when(missionQueryPort.findByScenarioIdOrderByOrderIndex(scenarioId)).thenReturn(List.of(m1, m2));

        var cmd = new ManageScenarioUseCase.ReorderMissionsCommand(scenarioId, List.of(m2.getId(), m1.getId()));
        service.reorderMissions(cmd);

        verify(missionCommandPort).setOrderIndex(m2.getId(), -3);
        verify(missionCommandPort).setOrderIndex(m1.getId(), -4);
        verify(missionCommandPort).setOrderIndex(m2.getId(), 1);
        verify(missionCommandPort).setOrderIndex(m1.getId(), 2);
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
                .ordered(false).theme(astronomyTheme).difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(scenarioId).orderIndex(1).enabled(true).build();
        when(scenarioRepository.findById(scenarioId)).thenReturn(Optional.of(
                new Scenario(scenarioId, "S", "D", astronomyTheme, 0, true)));
        when(missionQueryPort.findByScenarioIdOrderByOrderIndex(scenarioId)).thenReturn(List.of(m1));
        var cmd = new ManageScenarioUseCase.ReorderMissionsCommand(scenarioId, List.of());
        assertThatThrownBy(() -> service.reorderMissions(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("count mismatch");
    }

    @Test
    void countMissionsByScenarioId() {
        when(missionQueryPort.countByScenarioId(scenarioId)).thenReturn(5);
        assertThat(service.countMissionsByScenarioId(scenarioId)).isEqualTo(5);
    }
}
