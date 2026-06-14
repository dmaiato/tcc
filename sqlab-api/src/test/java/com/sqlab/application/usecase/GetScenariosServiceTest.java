package com.sqlab.application.usecase;

import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.domain.exception.ScenarioNotFoundException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetScenariosServiceTest {

    @Mock private ScenarioRepository scenarioRepository;

    private GetScenariosService service;

    @BeforeEach
    void setUp() {
        service = new GetScenariosService(scenarioRepository);
    }

    @Test
    void listAll() {
        var scenario = new Scenario(UUID.randomUUID(), "S1", "D", Theme.ASTRONOMY, true, 1);
        when(scenarioRepository.findAll()).thenReturn(List.of(scenario));
        var result = service.handle();
        assertThat(result).hasSize(1);
    }

    @Test
    void findById() {
        var id = UUID.randomUUID();
        var scenario = new Scenario(id, "S1", "D", Theme.ASTRONOMY, true, 1);
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
}
