package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetUserProgressUseCase;
import com.sqlab.application.port.out.ProgressRepository;
import com.sqlab.domain.model.Progress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserProgressServiceTest {

    @Mock private ProgressRepository progressRepository;

    private GetUserProgressService service;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new GetUserProgressService(progressRepository);
    }

    @Test
    void returnsEmptyWhenNoProgress() {
        when(progressRepository.findByUserId(userId)).thenReturn(List.of());
        var result = service.handle(new GetUserProgressUseCase.Query(userId));
        assertThat(result).isEmpty();
    }

    @Test
    void returnsProgressList() {
        var progress = Progress.complete(userId, UUID.randomUUID());
        when(progressRepository.findByUserId(userId)).thenReturn(List.of(progress));
        var result = service.handle(new GetUserProgressUseCase.Query(userId));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(userId);
        assertThat(result.get(0).isCompleted()).isTrue();
    }
}
