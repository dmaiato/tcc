package com.sqlab.application.usecase;

import com.sqlab.application.port.in.AdminValidateMissionUseCase;
import com.sqlab.application.port.out.MissionQueryPort;
import com.sqlab.domain.exception.MissionNotFoundException;
import com.sqlab.domain.model.ExpectedTuple;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.ValidationResult;
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
class AdminValidateMissionServiceTest {

    @Mock
    private MissionQueryPort missionQueryPort;

    private AdminValidateMissionService service;

    @BeforeEach
    void setUp() {
        service = new AdminValidateMissionService(missionQueryPort);
    }

    @Test
    void shouldDelegateToMissionValidate() {
        UUID missionId = UUID.randomUUID();
        List<Map<String, Object>> tuples = List.of(Map.of("name", "Alice"));

        Mission mission = mock(Mission.class);
        when(missionQueryPort.findById(missionId)).thenReturn(Optional.of(mission));
        when(mission.validate(tuples)).thenReturn(ValidationResult.CORRECT);

        ValidationResult result = service.handle(
                new AdminValidateMissionUseCase.Command(missionId, tuples));

        assertThat(result.correct()).isTrue();
        verify(mission).validate(tuples);
    }

    @Test
    void shouldThrowWhenMissionNotFound() {
        UUID missionId = UUID.randomUUID();
        when(missionQueryPort.findById(missionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.handle(
                new AdminValidateMissionUseCase.Command(missionId, List.of())))
                .isInstanceOf(MissionNotFoundException.class);
    }
}
