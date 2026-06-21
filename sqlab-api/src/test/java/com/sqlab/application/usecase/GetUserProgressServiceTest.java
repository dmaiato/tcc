package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetUserProgressUseCase;
import com.sqlab.application.port.out.MissionQueryPort;
import com.sqlab.application.port.out.ProgressRepository;
import com.sqlab.application.port.out.ScenarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserProgressServiceTest {

    @Mock private ProgressRepository progressRepository;
    @Mock private MissionQueryPort missionQueryPort;
    @Mock private ScenarioRepository scenarioRepository;

    private GetUserProgressService service;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new GetUserProgressService(progressRepository, missionQueryPort, scenarioRepository);
    }

    @Test
    void returnsEmptyWhenNoProgress() {
        when(progressRepository.findByUserId(userId)).thenReturn(List.of());
        var result = service.handle(new GetUserProgressUseCase.Query(userId));
        assertThat(result).isEmpty();
    }

    @Test
    void returnsProgressList() {
        var missionId = UUID.randomUUID();
        var completedAt = LocalDateTime.now();
        var progress = com.sqlab.domain.model.Progress.complete(userId, missionId);
        when(progressRepository.findByUserId(userId)).thenReturn(List.of(progress));
        when(missionQueryPort.findAllById(Set.of(missionId))).thenReturn(List.of());
        var result = service.handle(new GetUserProgressUseCase.Query(userId));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).missionId()).isEqualTo(missionId);
        assertThat(result.get(0).completed()).isTrue();
    }
}
