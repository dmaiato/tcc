package com.sqlab.application.usecase;

import com.sqlab.application.port.out.MissionQueryPort;
import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.domain.exception.ScenarioNotFoundException;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Scenario;
import com.sqlab.domain.model.Theme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAdminScenariosServiceTest {

    @Mock
    private ScenarioRepository scenarioRepository;
    @Mock
    private MissionQueryPort missionQueryPort;

    private GetAdminScenariosService service;

    @BeforeEach
    void setUp() {
        service = new GetAdminScenariosService(scenarioRepository, missionQueryPort);
    }

    @Test
    void listAllReturnsScenariosWithMissionCount() {
        var theme = new Theme(UUID.randomUUID(), "ASTRONOMY", null, null);
        var s1 = new Scenario(UUID.randomUUID(), "S1", "D1", theme, 1, true);
        var s2 = new Scenario(UUID.randomUUID(), "S2", "D2", theme, 2, true);

        when(scenarioRepository.findAll()).thenReturn(List.of(s1, s2));
        when(missionQueryPort.countByScenarioId(s1.getId())).thenReturn(3);
        when(missionQueryPort.countByScenarioId(s2.getId())).thenReturn(5);

        var results = service.listAll();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).totalMissions()).isEqualTo(3);
        assertThat(results.get(1).totalMissions()).isEqualTo(5);
    }

    @Test
    void findByIdReturnsScenarioWithMissions() {
        var id = UUID.randomUUID();
        var theme = new Theme(UUID.randomUUID(), "CRIMINAL", null, null);
        var scenario = new Scenario(id, "Night at the Blue Moon", "Desc", theme, 2, true);
        var missions = List.<Mission>of();

        when(scenarioRepository.findById(id)).thenReturn(Optional.of(scenario));
        when(missionQueryPort.findByScenarioIdOrderByOrderIndex(id)).thenReturn(missions);

        var result = service.findById(id);

        assertThat(result.scenario()).isEqualTo(scenario);
        assertThat(result.missions()).isSameAs(missions);
    }

    @Test
    void findByIdThrowsWhenNotFound() {
        var id = UUID.randomUUID();
        when(scenarioRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(ScenarioNotFoundException.class);
    }

    @Test
    void listAllPaginatedReturnsPage() {
        var theme = new Theme(UUID.randomUUID(), "ASTRONOMY", null, null);
        var scenario = new Scenario(UUID.randomUUID(), "Paginated", "D", theme, 1, true);
        var domainPage = new com.sqlab.domain.model.Page<>(List.of(scenario), 1, 1, 0, 12);

        when(scenarioRepository.findAllByFilters(isNull(), isNull(), isNull(), eq(0), eq(12))).thenReturn(domainPage);
        when(missionQueryPort.countByScenarioId(scenario.getId())).thenReturn(3);

        var result = service.listAll(null, null, null, 0, 12);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).scenario().getTitle()).isEqualTo("Paginated");
        assertThat(result.content().get(0).totalMissions()).isEqualTo(3);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    void listAllPaginatedFiltersByNameAndTheme() {
        var theme = new Theme(UUID.randomUUID(), "ASTRONOMY", null, null);
        var scenario = new Scenario(UUID.randomUUID(), "Astro Scenario", "D", theme, 1, true);
        var domainPage = new com.sqlab.domain.model.Page<>(List.of(scenario), 1, 1, 0, 12);

        when(scenarioRepository.findAllByFilters(eq("astro"), eq("ASTRONOMY"), isNull(), eq(0), eq(12))).thenReturn(domainPage);
        when(missionQueryPort.countByScenarioId(scenario.getId())).thenReturn(5);

        var result = service.listAll("astro", "ASTRONOMY", null, 0, 12);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).scenario().getTitle()).isEqualTo("Astro Scenario");
        assertThat(result.content().get(0).totalMissions()).isEqualTo(5);
    }
}
