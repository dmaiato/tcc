package com.sqlab.infrastructure.adapter.in.web;

import tools.jackson.databind.ObjectMapper;
import com.sqlab.application.port.in.GetAdminScenariosUseCase;
import com.sqlab.application.port.in.GetScenariosUseCase;
import com.sqlab.application.port.in.ManageScenarioUseCase;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
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
    private GetAdminScenariosUseCase getAdminScenariosUseCase;

    private final Theme astronomyTheme = new Theme(UUID.randomUUID(), "ASTRONOMY", null, null);

    @Test
    void listAll_shouldReturnScenarios() throws Exception {
        var scenarioId = UUID.randomUUID();
        var summary = new GetScenariosUseCase.ScenarioSummaryResult(scenarioId, "Scenario 1", 1, 0, 1, astronomyTheme);
        when(getScenariosUseCase.handleEnabledWithProgress(any())).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/scenarios").with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Scenario 1"))
                .andExpect(jsonPath("$[0].totalMissions").value(1))
                .andExpect(jsonPath("$[0].completedMissions").value(0));
    }

    @Test
    void listAll_shouldFilterOutScenariosWithDisabledMissions() throws Exception {
        when(getScenariosUseCase.handleEnabledWithProgress(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/scenarios").with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void listAllPaginated_shouldReturnScenarioPage() throws Exception {
        var scenarioId = UUID.randomUUID();
        var summary = new GetScenariosUseCase.ScenarioSummaryResult(scenarioId, "Scenario 1", 1, 0, 1, astronomyTheme);
        var page = new com.sqlab.domain.model.Page<>(List.of(summary), 1, 1, 0, 12);

        when(getScenariosUseCase.handleEnabledWithProgress(any(), isNull(), isNull(), eq(0), eq(12))).thenReturn(page);

        mockMvc.perform(get("/api/scenarios?page=0")
                        .with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Scenario 1"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(12))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void listAllPaginated_shouldFilterByName() throws Exception {
        var scenarioId = UUID.randomUUID();
        var summary = new GetScenariosUseCase.ScenarioSummaryResult(scenarioId, "Astro", 1, 0, 1, astronomyTheme);
        var page = new com.sqlab.domain.model.Page<>(List.of(summary), 1, 1, 0, 12);

        when(getScenariosUseCase.handleEnabledWithProgress(any(), eq("astro"), isNull(), eq(0), eq(12))).thenReturn(page);

        mockMvc.perform(get("/api/scenarios?page=0&name=astro")
                        .with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Astro"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void listAllPaginated_shouldFilterByTheme() throws Exception {
        var scenarioId = UUID.randomUUID();
        var summary = new GetScenariosUseCase.ScenarioSummaryResult(scenarioId, "Astro", 1, 0, 1, astronomyTheme);
        var page = new com.sqlab.domain.model.Page<>(List.of(summary), 1, 1, 0, 12);

        when(getScenariosUseCase.handleEnabledWithProgress(any(), isNull(), eq("ASTRONOMY"), eq(0), eq(12))).thenReturn(page);

        mockMvc.perform(get("/api/scenarios?page=0&theme=ASTRONOMY")
                        .with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Astro"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void findById_shouldReturnScenarioDetail() throws Exception {
        var scenarioId = UUID.randomUUID();
        var userUuid = UUID.fromString(USER_ID);
        var scenario = new Scenario(scenarioId, "Detail", "Description", astronomyTheme, 1, true);
        var mission = Mission.builder()
                .id(UUID.randomUUID()).title("M1").briefing("B").objective("O")
                .hint(null).ddlScript("DDL").dmlScript(null)
                .techniques(List.of(new Technique(null, "SELECT"))).xpReward(100)
                .expectedResult(new ExpectedTuple(List.of()))
                .ordered(true).theme(astronomyTheme).difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(scenarioId).orderIndex(1).enabled(true).requiredLevel(1)
                .build();

        var detail = new GetScenariosUseCase.ScenarioDetailResult(scenario, List.of(new GetScenariosUseCase.MissionStatus(mission, "AVAILABLE")), 0, 1);
        when(getScenariosUseCase.handleDetail(scenarioId, userUuid)).thenReturn(detail);

        mockMvc.perform(get("/api/scenarios/{id}", scenarioId).with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Detail"))
                .andExpect(jsonPath("$.missions[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.userProgress.completedCount").value(0));
    }

    @Test
    void findById_shouldThrow404WhenNotFound() throws Exception {
        var scenarioId = UUID.randomUUID();
        var userUuid = UUID.fromString(USER_ID);
        when(getScenariosUseCase.handleDetail(scenarioId, userUuid)).thenThrow(new ScenarioNotFoundException(scenarioId));

        mockMvc.perform(get("/api/scenarios/{id}", scenarioId).with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null))))
                .andExpect(status().isNotFound());
    }

    @Test
    void findById_shouldMarkMissionAsCompleted() throws Exception {
        var scenarioId = UUID.randomUUID();
        var userUuid = UUID.fromString(USER_ID);
        var missionId = UUID.randomUUID();
        var scenario = new Scenario(scenarioId, "Completed Scenario", "Description", astronomyTheme, 1, true);
        var mission = Mission.builder()
                .id(missionId).title("M1").briefing("B").objective("O")
                .hint(null).ddlScript("DDL").dmlScript(null)
                .techniques(List.of()).xpReward(100)
                .expectedResult(new ExpectedTuple(List.of()))
                .ordered(true).theme(astronomyTheme).difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(scenarioId).orderIndex(1).enabled(true).requiredLevel(1)
                .build();

        var detail = new GetScenariosUseCase.ScenarioDetailResult(scenario, List.of(new GetScenariosUseCase.MissionStatus(mission, "COMPLETED")), 1, 1);
        when(getScenariosUseCase.handleDetail(scenarioId, userUuid)).thenReturn(detail);

        mockMvc.perform(get("/api/scenarios/{id}", scenarioId).with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Completed Scenario"))
                .andExpect(jsonPath("$.missions[0].status").value("COMPLETED"));
    }

    @Test
    void listAllAdmin_shouldReturnPaginatedScenarios() throws Exception {
        var scenario = new Scenario(UUID.randomUUID(), "Admin View", "Desc", astronomyTheme, 1, true);
        var result = new GetAdminScenariosUseCase.ScenarioListResult(scenario, 0);
        var page = new com.sqlab.domain.model.Page<>(List.of(result), 1, 1, 0, 12);

        when(getAdminScenariosUseCase.listAll(isNull(), isNull(), isNull(), eq(0), eq(12))).thenReturn(page);

        mockMvc.perform(get("/api/admin/scenarios?page=0").with(user(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Admin View"))
                .andExpect(jsonPath("$.content[0].totalMissions").value(0))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void listAllAdmin_shouldFilterByNameAndTheme() throws Exception {
        var scenario = new Scenario(UUID.randomUUID(), "Filtered", "Desc", astronomyTheme, 1, true);
        var result = new GetAdminScenariosUseCase.ScenarioListResult(scenario, 3);
        var page = new com.sqlab.domain.model.Page<>(List.of(result), 1, 1, 0, 12);

        when(getAdminScenariosUseCase.listAll(eq("space"), eq("ASTRONOMY"), isNull(), eq(0), eq(12))).thenReturn(page);

        mockMvc.perform(get("/api/admin/scenarios?page=0")
                        .param("name", "space")
                        .param("theme", "ASTRONOMY")
                        .with(user(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Filtered"))
                .andExpect(jsonPath("$.content[0].totalMissions").value(3));
    }

    @Test
    void listAllAdmin_shouldUseDefaultPagination() throws Exception {
        var scenario = new Scenario(UUID.randomUUID(), "Default", "Desc", astronomyTheme, 1, true);
        var result = new GetAdminScenariosUseCase.ScenarioListResult(scenario, 0);
        var page = new com.sqlab.domain.model.Page<>(List.of(result), 1, 1, 0, 12);

        when(getAdminScenariosUseCase.listAll(isNull(), isNull(), isNull(), eq(0), eq(12))).thenReturn(page);

        mockMvc.perform(get("/api/admin/scenarios?page=0").with(user(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(12));
    }

    @Test
    void findByIdAdmin_shouldReturnAdminDetail() throws Exception {
        var scenarioId = UUID.randomUUID();
        var scenario = new Scenario(scenarioId, "Admin Detail", "Desc", astronomyTheme, 1, true);
        var mission = Mission.builder()
                .id(UUID.randomUUID()).title("M1").briefing("B").objective("O")
                .hint(null).ddlScript("DDL").dmlScript(null)
                .techniques(List.of()).xpReward(100)
                .expectedResult(new ExpectedTuple(List.of()))
                .ordered(true).theme(astronomyTheme).difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(scenarioId).orderIndex(1).enabled(true).requiredLevel(1)
                .build();

        var result = new GetAdminScenariosUseCase.ScenarioDetailResult(scenario, List.of(mission));
        when(getAdminScenariosUseCase.findById(scenarioId)).thenReturn(result);

        mockMvc.perform(get("/api/admin/scenarios/{id}", scenarioId).with(user(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Admin Detail"))
                .andExpect(jsonPath("$.missions[0].title").value("M1"));
    }

    @Test
    void create_shouldReturnCreatedScenario() throws Exception {
        var scenarioId = UUID.randomUUID();
        var scenario = new Scenario(scenarioId, "New Scenario", "Desc", astronomyTheme, 1, true);
        when(manageScenarioUseCase.create(any())).thenReturn(scenario);

        var req = new ScenarioDto.CreateScenarioRequest("New Scenario", "Desc", "ASTRONOMY", true, 1);

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
        var scenario = new Scenario(scenarioId, "Updated", "New Desc", astronomyTheme, 2, true);
        when(manageScenarioUseCase.update(any())).thenReturn(scenario);
        when(manageScenarioUseCase.countMissionsByScenarioId(scenarioId)).thenReturn(5);

        var req = new ScenarioDto.UpdateScenarioRequest("Updated", "New Desc", "BIOLOGY", true, 2);

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
