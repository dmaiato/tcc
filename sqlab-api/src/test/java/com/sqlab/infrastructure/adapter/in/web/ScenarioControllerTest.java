package com.sqlab.infrastructure.adapter.in.web;

import tools.jackson.databind.ObjectMapper;
import com.sqlab.application.port.in.GetMissionsUseCase;
import com.sqlab.application.port.in.GetScenariosUseCase;
import com.sqlab.application.port.in.ManageScenarioUseCase;
import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.application.port.out.ProgressRepository;
import com.sqlab.application.port.out.UserRepository;
import com.sqlab.domain.exception.ScenarioNotFoundException;
import com.sqlab.domain.model.*;
import com.sqlab.infrastructure.adapter.in.web.dto.ScenarioDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScenarioController.class)
@Import(TestSecurityConfig.class)
class ScenarioControllerTest {

    private static final String USER_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GetScenariosUseCase getScenariosUseCase;

    @MockitoBean
    private ManageScenarioUseCase manageScenarioUseCase;

    @MockitoBean
    private MissionRepository missionRepository;

    @MockitoBean
    private ProgressRepository progressRepository;

    @MockitoBean
    private UserRepository userRepository;

    private Scenario createScenario(UUID id, String title) {
        return new Scenario(id, title, "Description", Theme.ASTRONOMY, true, 1);
    }

    @Test
    void listAll_shouldReturnScenarios() throws Exception {
        var scenarioId = UUID.randomUUID();
        var scenario = createScenario(scenarioId, "Scenario 1");
        var mission = Mission.builder()
                .id(UUID.randomUUID()).title("M1").briefing("B").objective("O")
                .hint(null).ddlScript("DDL").dmlScript(null)
                .techniques(List.of()).xpReward(100)
                .expectedResult(new ExpectedTuple(List.of()))
                .ordered(true).theme(Theme.ASTRONOMY).difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(scenarioId).orderIndex(1).enabled(true).requiredLevel(1)
                .build();

        when(getScenariosUseCase.handleEnabled()).thenReturn(List.of(scenario));
        when(progressRepository.findCompletedMissionIdsByUserId(any())).thenReturn(Set.of());
        when(missionRepository.findByScenarioIdInOrderByOrderIndex(any())).thenReturn(List.of(mission));

        mockMvc.perform(get("/api/scenarios").with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Scenario 1"))
                .andExpect(jsonPath("$[0].totalMissions").value(1))
                .andExpect(jsonPath("$[0].completedMissions").value(0));
    }

    @Test
    void listAll_shouldFilterOutScenariosWithDisabledMissions() throws Exception {
        var scenarioId = UUID.randomUUID();
        var scenario = createScenario(scenarioId, "Disabled");
        var disabledMission = Mission.builder()
                .id(UUID.randomUUID()).title("M1").briefing("B").objective("O")
                .hint(null).ddlScript("DDL").dmlScript(null)
                .techniques(List.of()).xpReward(100)
                .expectedResult(new ExpectedTuple(List.of()))
                .ordered(true).theme(Theme.ASTRONOMY).difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(scenarioId).orderIndex(1).enabled(false).requiredLevel(1)
                .build();

        when(getScenariosUseCase.handleEnabled()).thenReturn(List.of(scenario));
        when(missionRepository.findByScenarioIdInOrderByOrderIndex(any())).thenReturn(List.of(disabledMission));

        mockMvc.perform(get("/api/scenarios").with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void findById_shouldReturnScenarioDetail() throws Exception {
        var scenarioId = UUID.randomUUID();
        var scenario = createScenario(scenarioId, "Detail");
        var mission = Mission.builder()
                .id(UUID.randomUUID()).title("M1").briefing("B").objective("O")
                .hint(null).ddlScript("DDL").dmlScript(null)
                .techniques(List.of("SELECT")).xpReward(100)
                .expectedResult(new ExpectedTuple(List.of()))
                .ordered(true).theme(Theme.ASTRONOMY).difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(scenarioId).orderIndex(1).enabled(true).requiredLevel(1)
                .build();

        when(getScenariosUseCase.handle(scenarioId)).thenReturn(scenario);
        when(progressRepository.findCompletedMissionIdsByUserId(any())).thenReturn(Set.of());
        when(missionRepository.findByScenarioIdOrderByOrderIndex(scenarioId)).thenReturn(List.of(mission));
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/scenarios/{id}", scenarioId).with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Detail"))
                .andExpect(jsonPath("$.missions[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.userProgress.completedCount").value(0));
    }

    @Test
    void findById_shouldThrow404WhenNotFound() throws Exception {
        var scenarioId = UUID.randomUUID();
        when(getScenariosUseCase.handle(scenarioId)).thenThrow(new ScenarioNotFoundException(scenarioId));

        mockMvc.perform(get("/api/scenarios/{id}", scenarioId).with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null))))
                .andExpect(status().isNotFound());
    }

    @Test
    void findById_shouldMarkMissionAsCompleted() throws Exception {
        var scenarioId = UUID.randomUUID();
        var missionId = UUID.randomUUID();
        var scenario = createScenario(scenarioId, "Completed Scenario");
        var mission = Mission.builder()
                .id(missionId).title("M1").briefing("B").objective("O")
                .hint(null).ddlScript("DDL").dmlScript(null)
                .techniques(List.of()).xpReward(100)
                .expectedResult(new ExpectedTuple(List.of()))
                .ordered(true).theme(Theme.ASTRONOMY).difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(scenarioId).orderIndex(1).enabled(true).requiredLevel(1)
                .build();

        when(getScenariosUseCase.handle(scenarioId)).thenReturn(scenario);
        when(progressRepository.findCompletedMissionIdsByUserId(any())).thenReturn(Set.of(missionId));
        when(missionRepository.findByScenarioIdOrderByOrderIndex(scenarioId)).thenReturn(List.of(mission));
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/scenarios/{id}", scenarioId).with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Completed Scenario"))
                .andExpect(jsonPath("$.missions[0].status").value("COMPLETED"));
    }

    @Test
    void listAllAdmin_shouldReturnAllScenarios() throws Exception {
        var scenario = createScenario(UUID.randomUUID(), "Admin View");
        when(getScenariosUseCase.handle()).thenReturn(List.of(scenario));
        when(missionRepository.countByScenarioId(any())).thenReturn(3);

        mockMvc.perform(get("/api/admin/scenarios").with(user(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Admin View"))
                .andExpect(jsonPath("$[0].totalMissions").value(3));
    }

    @Test
    void findByIdAdmin_shouldReturnAdminDetail() throws Exception {
        var scenarioId = UUID.randomUUID();
        var scenario = createScenario(scenarioId, "Admin Detail");
        var mission = Mission.builder()
                .id(UUID.randomUUID()).title("M1").briefing("B").objective("O")
                .hint(null).ddlScript("DDL").dmlScript(null)
                .techniques(List.of()).xpReward(100)
                .expectedResult(new ExpectedTuple(List.of()))
                .ordered(true).theme(Theme.ASTRONOMY).difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(scenarioId).orderIndex(1).enabled(true).requiredLevel(1)
                .build();

        when(getScenariosUseCase.handle(scenarioId)).thenReturn(scenario);
        when(missionRepository.findByScenarioIdOrderByOrderIndex(scenarioId)).thenReturn(List.of(mission));

        mockMvc.perform(get("/api/admin/scenarios/{id}", scenarioId).with(user(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Admin Detail"))
                .andExpect(jsonPath("$.missions[0].title").value("M1"));
    }

    @Test
    void create_shouldReturnCreatedScenario() throws Exception {
        var scenarioId = UUID.randomUUID();
        var scenario = createScenario(scenarioId, "New Scenario");
        when(manageScenarioUseCase.create(any())).thenReturn(scenario);

        var req = new ScenarioDto.CreateScenarioRequest("New Scenario", "Desc", Theme.ASTRONOMY, true, 1);

        mockMvc.perform(post("/api/admin/scenarios")
                        .with(user(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Scenario"))
                .andExpect(jsonPath("$.totalMissions").value(0));
    }

    @Test
    void create_shouldReturn400WhenValidationFails() throws Exception {
        var body = """
                {"requiredLevel":0}
                """;

        mockMvc.perform(post("/api/admin/scenarios")
                        .with(user(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturnUpdatedScenario() throws Exception {
        var scenarioId = UUID.randomUUID();
        var scenario = createScenario(scenarioId, "Updated");
        when(manageScenarioUseCase.update(any())).thenReturn(scenario);
        when(missionRepository.countByScenarioId(scenarioId)).thenReturn(5);

        var req = new ScenarioDto.UpdateScenarioRequest("Updated", "New Desc", Theme.BIOLOGY, true, 2);

        mockMvc.perform(put("/api/admin/scenarios/{id}", scenarioId)
                        .with(user(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"))
                .andExpect(jsonPath("$.totalMissions").value(5));
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/admin/scenarios/{id}", UUID.randomUUID()).with(user(USER_ID)))
                .andExpect(status().isNoContent());
    }

    @Test
    void reorderMissions_shouldReturnOk() throws Exception {
        var missionId = UUID.randomUUID();
        var req = new ScenarioDto.ReorderMissionsRequest(List.of(missionId));

        mockMvc.perform(put("/api/admin/scenarios/{id}/missions/reorder", UUID.randomUUID())
                        .with(user(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
