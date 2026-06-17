package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetUserSkillsUseCase;
import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.application.port.out.ProgressRepository;
import com.sqlab.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserSkillsServiceTest {

    @Mock private ProgressRepository progressRepository;
    @Mock private MissionRepository missionRepository;

    private GetUserSkillsService service;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new GetUserSkillsService(progressRepository, missionRepository);
    }

    @Test
    void returnsEmptyWhenNoCompletedMissions() {
        when(progressRepository.findCompletedMissionIdsByUserId(userId)).thenReturn(Set.of());
        var result = service.handle(new GetUserSkillsUseCase.Query(userId));
        assertThat(result).isEmpty();
    }

    @Test
    void returnsUniqueSortedSkills() {
        var missionId = UUID.randomUUID();
        when(progressRepository.findCompletedMissionIdsByUserId(userId)).thenReturn(Set.of(missionId));

        var mission = Mission.builder().id(missionId).title("M").briefing("B").objective("O")
                .ddlScript("DDL").techniques(List.of(new Technique(null, "JOIN"), new Technique(null, "SELECT"))).xpReward(10)
                .expectedResult(new ExpectedTuple(List.of(Map.of("x", 1))))
                .ordered(false).theme(new Theme(UUID.randomUUID(), "ASTRONOMY", null, null)).difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(null).orderIndex(null).enabled(true).build();
        when(missionRepository.findAllById(Set.of(missionId))).thenReturn(List.of(mission));

        var result = service.handle(new GetUserSkillsUseCase.Query(userId));
        assertThat(result).containsExactly("JOIN", "SELECT");
    }

    @Test
    void handlesMultipleMissionsWithSharedSkills() {
        var m1 = UUID.randomUUID();
        var m2 = UUID.randomUUID();
        when(progressRepository.findCompletedMissionIdsByUserId(userId)).thenReturn(Set.of(m1, m2));
        when(missionRepository.findAllById(Set.of(m1, m2))).thenReturn(List.of(
                Mission.builder().id(m1).title("M1").briefing("B").objective("O")
                        .ddlScript("DDL").techniques(List.of(new Technique(null, "SELECT"), new Technique(null, "JOIN"))).xpReward(10)
                        .expectedResult(new ExpectedTuple(List.of(Map.of("x", 1))))
                        .ordered(false).theme(new Theme(UUID.randomUUID(), "ASTRONOMY", null, null)).difficulty(DifficultyLevel.BEGINNER)
                        .scenarioId(null).orderIndex(null).enabled(true).build(),
                Mission.builder().id(m2).title("M2").briefing("B").objective("O")
                        .ddlScript("DDL").techniques(List.of(new Technique(null, "JOIN"), new Technique(null, "GROUP BY"))).xpReward(10)
                        .expectedResult(new ExpectedTuple(List.of(Map.of("x", 1))))
                        .ordered(false).theme(new Theme(UUID.randomUUID(), "ASTRONOMY", null, null)).difficulty(DifficultyLevel.BEGINNER)
                        .scenarioId(null).orderIndex(null).enabled(true).build()
        ));

        var result = service.handle(new GetUserSkillsUseCase.Query(userId));
        assertThat(result).containsExactly("GROUP BY", "JOIN", "SELECT");
    }
}
