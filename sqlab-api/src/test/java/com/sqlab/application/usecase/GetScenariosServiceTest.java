package com.sqlab.application.usecase;

import com.sqlab.application.port.out.MissionQueryPort;
import com.sqlab.application.port.out.MissionValidationPort;
import com.sqlab.application.port.out.ProgressRepository;
import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.application.port.out.UserRepository;
import com.sqlab.domain.exception.ScenarioNotFoundException;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Page;
import com.sqlab.domain.model.Scenario;
import com.sqlab.domain.model.Theme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetScenariosServiceTest {

    @Mock private ScenarioRepository scenarioRepository;
    @Mock private MissionQueryPort missionQueryPort;
    @Mock private MissionValidationPort missionValidationPort;
    @Mock private ProgressRepository progressRepository;
    @Mock private UserRepository userRepository;

    private GetScenariosService service;

    private final Theme astronomy = new Theme(UUID.randomUUID(), "ASTRONOMY", null, null);

    @BeforeEach
    void setUp() {
        service = new GetScenariosService(scenarioRepository, missionQueryPort, missionValidationPort, progressRepository, userRepository);
    }

    @Test
    void listAll() {
        var scenario = new Scenario(UUID.randomUUID(), "S1", "D", astronomy, 1, true);
        when(scenarioRepository.findAll()).thenReturn(List.of(scenario));
        var result = service.handle();
        assertThat(result).hasSize(1);
    }

    @Test
    void findById() {
        var id = UUID.randomUUID();
        var scenario = new Scenario(id, "S1", "D", astronomy, 1, true);
        when(scenarioRepository.findById(id)).thenReturn(Optional.of(scenario));
        var result = service.handle(id);
        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void findByIdThrowsWhenNotFound() {
        var id = UUID.randomUUID();
        when(scenarioRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.handle(id)).isInstanceOf(ScenarioNotFoundException.class);
    }

    @Test
    void handleEnabledWithProgressPaginated() {
        var userId = UUID.randomUUID();
        var scenarioId = UUID.randomUUID();
        var missionId = UUID.randomUUID();
        var scenario = new Scenario(scenarioId, "S1", "D", astronomy, 1, true);
        var mission = Mission.builder()
                .id(missionId).title("M1").briefing("B").objective("O")
                .ddlScript("DDL").techniques(List.of()).xpReward(100)
                .expectedResult(new com.sqlab.domain.model.ExpectedTuple(List.of()))
                .ordered(true).theme(astronomy)
                .difficulty(com.sqlab.domain.model.DifficultyLevel.BEGINNER)
                .scenarioId(scenarioId).orderIndex(1).enabled(true).requiredLevel(1)
                .build();
        var domainPage = new Page<>(List.of(scenario), 1, 1, 0, 12);

        when(scenarioRepository.findByFilters(null, null, 0, 12)).thenReturn(domainPage);
        when(missionQueryPort.findByScenarioIdInOrderByOrderIndex(Set.of(scenarioId))).thenReturn(List.of(mission));
        when(progressRepository.findCompletedMissionIdsByUserId(userId)).thenReturn(Set.of());

        var result = service.handleEnabledWithProgress(userId, null, null, 0, 12);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).title()).isEqualTo("S1");
        assertThat(result.content().get(0).completedMissions()).isZero();
        assertThat(result.content().get(0).totalMissions()).isEqualTo(1);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    void handleEnabledWithProgressPaginatedWithNameFilter() {
        var userId = UUID.randomUUID();
        var scenarioId = UUID.randomUUID();
        var missionId = UUID.randomUUID();
        var scenario = new Scenario(scenarioId, "Astro Quest", "D", astronomy, 1, true);
        var mission = Mission.builder()
                .id(missionId).title("M1").briefing("B").objective("O")
                .ddlScript("DDL").techniques(List.of()).xpReward(100)
                .expectedResult(new com.sqlab.domain.model.ExpectedTuple(List.of()))
                .ordered(true).theme(astronomy)
                .difficulty(com.sqlab.domain.model.DifficultyLevel.BEGINNER)
                .scenarioId(scenarioId).orderIndex(1).enabled(true).requiredLevel(1)
                .build();
        var domainPage = new Page<>(List.of(scenario), 1, 1, 0, 12);

        when(scenarioRepository.findByFilters("astro", null, 0, 12)).thenReturn(domainPage);
        when(missionQueryPort.findByScenarioIdInOrderByOrderIndex(Set.of(scenarioId))).thenReturn(List.of(mission));
        when(progressRepository.findCompletedMissionIdsByUserId(userId)).thenReturn(Set.of());

        var result = service.handleEnabledWithProgress(userId, "astro", null, 0, 12);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).title()).isEqualTo("Astro Quest");
    }

    @Test
    void handleEnabledWithProgressPaginatedWithThemeFilter() {
        var userId = UUID.randomUUID();
        var scenarioId = UUID.randomUUID();
        var missionId = UUID.randomUUID();
        var scenario = new Scenario(scenarioId, "Bio One", "D", astronomy, 1, true);
        var mission = Mission.builder()
                .id(missionId).title("M1").briefing("B").objective("O")
                .ddlScript("DDL").techniques(List.of()).xpReward(100)
                .expectedResult(new com.sqlab.domain.model.ExpectedTuple(List.of()))
                .ordered(true).theme(astronomy)
                .difficulty(com.sqlab.domain.model.DifficultyLevel.BEGINNER)
                .scenarioId(scenarioId).orderIndex(1).enabled(true).requiredLevel(1)
                .build();
        var domainPage = new Page<>(List.of(scenario), 1, 1, 0, 12);

        when(scenarioRepository.findByFilters(null, "ASTRONOMY", 0, 12)).thenReturn(domainPage);
        when(missionQueryPort.findByScenarioIdInOrderByOrderIndex(Set.of(scenarioId))).thenReturn(List.of(mission));
        when(progressRepository.findCompletedMissionIdsByUserId(userId)).thenReturn(Set.of());

        var result = service.handleEnabledWithProgress(userId, null, "ASTRONOMY", 0, 12);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).title()).isEqualTo("Bio One");
    }

    @Test
    void handleEnabledWithProgressPaginatedWithCompletedMissions() {
        var userId = UUID.randomUUID();
        var scenarioId = UUID.randomUUID();
        var missionId = UUID.randomUUID();
        var scenario = new Scenario(scenarioId, "Done", "D", astronomy, 1, true);
        var mission = Mission.builder()
                .id(missionId).title("M1").briefing("B").objective("O")
                .ddlScript("DDL").techniques(List.of()).xpReward(100)
                .expectedResult(new com.sqlab.domain.model.ExpectedTuple(List.of()))
                .ordered(true).theme(astronomy)
                .difficulty(com.sqlab.domain.model.DifficultyLevel.BEGINNER)
                .scenarioId(scenarioId).orderIndex(1).enabled(true).requiredLevel(1)
                .build();
        var domainPage = new Page<>(List.of(scenario), 1, 1, 0, 12);

        when(scenarioRepository.findByFilters(null, null, 0, 12)).thenReturn(domainPage);
        when(missionQueryPort.findByScenarioIdInOrderByOrderIndex(Set.of(scenarioId))).thenReturn(List.of(mission));
        when(progressRepository.findCompletedMissionIdsByUserId(userId)).thenReturn(Set.of(missionId));

        var result = service.handleEnabledWithProgress(userId, null, null, 0, 12);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).completedMissions()).isEqualTo(1);
    }
}
