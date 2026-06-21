package com.sqlab.application.usecase;

import com.sqlab.application.port.out.MissionQueryPort;
import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.domain.exception.MissionNotFoundException;
import com.sqlab.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAdminMissionsServiceTest {

    @Mock
    private MissionQueryPort missionQueryPort;
    @Mock
    private ScenarioRepository scenarioRepository;

    private GetAdminMissionsService service;

    @BeforeEach
    void setUp() {
        service = new GetAdminMissionsService(missionQueryPort, scenarioRepository);
    }

    @Test
    void listAllReturnsAllMissions() {
        var m1 = createMission(UUID.randomUUID(), "M1", null);
        var m2 = createMission(UUID.randomUUID(), "M2", UUID.randomUUID());

        when(missionQueryPort.findAll()).thenReturn(List.of(m1, m2));

        var results = service.listAll();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).mission().getTitle()).isEqualTo("M1");
        assertThat(results.get(1).mission().getTitle()).isEqualTo("M2");
    }

    @Test
    void findByIdReturnsMission() {
        var id = UUID.randomUUID();
        var mission = createMission(id, "Test Mission", null);

        when(missionQueryPort.findById(id)).thenReturn(Optional.of(mission));

        var result = service.findById(id);

        assertThat(result.mission().getTitle()).isEqualTo("Test Mission");
    }

    @Test
    void findByIdThrowsWhenNotFound() {
        var id = UUID.randomUUID();
        when(missionQueryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(MissionNotFoundException.class);
    }

    @Test
    void resultIncludesScenarioTitleAndTotalMissions() {
        var scenarioId = UUID.randomUUID();
        var scenario = new Scenario(scenarioId, "Blue Moon", "Desc",
                new Theme(UUID.randomUUID(), "CRIMINAL", null, null), true, 2);
        var mission = createMission(UUID.randomUUID(), "Mission X", scenarioId);

        when(missionQueryPort.findAll()).thenReturn(List.of(mission));
        when(scenarioRepository.findById(scenarioId)).thenReturn(Optional.of(scenario));
        when(missionQueryPort.countByScenarioIdAndEnabledTrue(scenarioId)).thenReturn(7);

        var results = service.listAll();

        assertThat(results.get(0).scenarioTitle()).isEqualTo("Blue Moon");
        assertThat(results.get(0).scenarioTotalMissions()).isEqualTo(7);
    }

    @Test
    void resultHasNullScenarioFieldsWhenMissionHasNoScenario() {
        var mission = createMission(UUID.randomUUID(), "Standalone", null);

        when(missionQueryPort.findAll()).thenReturn(List.of(mission));

        var results = service.listAll();

        assertThat(results.get(0).scenarioTitle()).isNull();
        assertThat(results.get(0).scenarioTotalMissions()).isNull();
    }

    private Mission createMission(UUID id, String title, UUID scenarioId) {
        return Mission.builder()
                .id(id)
                .title(title)
                .briefing("B")
                .objective("O")
                .ddlScript("DDL")
                .techniques(List.of())
                .xpReward(100)
                .expectedResult(new ExpectedTuple(List.of(Map.of("x", 1))))
                .ordered(false)
                .theme(new Theme(UUID.randomUUID(), "ASTRONOMY", null, null))
                .difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(scenarioId)
                .orderIndex(scenarioId != null ? 1 : null)
                .enabled(true)
                .requiredLevel(0)
                .build();
    }
}
