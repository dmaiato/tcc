package com.sqlab.infrastructure.adapter.in.web;

import tools.jackson.databind.ObjectMapper;
import com.sqlab.application.port.in.*;
import com.sqlab.domain.model.*;
import com.sqlab.domain.exception.MissionNotFoundException;
import com.sqlab.domain.model.Mission;
import com.sqlab.infrastructure.adapter.in.web.dto.MissionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MissionController.class)
@Import(TestSecurityConfig.class)
class MissionControllerTest {

    private static final String USER_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GetMissionsUseCase getMissionsUseCase;

    @MockitoBean
    private ValidateMissionUseCase validateMissionUseCase;

    @MockitoBean
    private ManageMissionUseCase manageMissionUseCase;

    @MockitoBean
    private AdminValidateMissionUseCase adminValidateMissionUseCase;

    @MockitoBean
    private GetAdminMissionsUseCase getAdminMissionsUseCase;

    private Mission createMission(UUID id, String title) {
        return Mission.builder()
                .id(id).title(title).briefing("Brief").objective("Obj")
                .hint(null).ddlScript("DDL").dmlScript(null)
                .techniques(List.of(new Technique(null, "SELECT"))).xpReward(100)
                .expectedResult(new ExpectedTuple(List.of()))
                .ordered(true).theme(new Theme(UUID.randomUUID(), "ASTRONOMY", null, null)).difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(null).orderIndex(null).enabled(true).requiredLevel(1)
                .build();
    }

    @Test
    void listAll_shouldReturnMissions() throws Exception {
        var mission = createMission(UUID.randomUUID(), "M1");
        when(getMissionsUseCase.handle(any(GetMissionsUseCase.ListAllQuery.class))).thenReturn(List.of(mission));

        mockMvc.perform(get("/api/missions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("M1"))
                .andExpect(jsonPath("$[0].theme.name").value("ASTRONOMY"))
                .andExpect(jsonPath("$[0].difficulty").value("BEGINNER"));
    }

    @Test
    void listAll_shouldFilterByThemeAndDifficulty() throws Exception {
        var mission = createMission(UUID.randomUUID(), "Filtered");
        when(getMissionsUseCase.handle(any(GetMissionsUseCase.ListAllQuery.class))).thenReturn(List.of(mission));
        mockMvc.perform(get("/api/missions")
                        .param("theme", "ASTRONOMY")
                        .param("difficulty", "BEGINNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Filtered"));
    }

    @Test
    void findById_shouldReturnMission() throws Exception {
        var missionId = UUID.randomUUID();
        var mission = createMission(missionId, "Detail Mission");
        var detail = new GetMissionsUseCase.MissionDetail(mission, 5, "Scenario Title");
        when(getMissionsUseCase.handleDetail(any())).thenReturn(detail);

        mockMvc.perform(get("/api/missions/{id}", missionId).with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Detail Mission"))
                .andExpect(jsonPath("$.scenarioTitle").value("Scenario Title"));
    }

    @Test
    void findById_shouldReturn404WhenNotFound() throws Exception {
        when(getMissionsUseCase.handleDetail(any())).thenThrow(new MissionNotFoundException(UUID.randomUUID()));

        mockMvc.perform(get("/api/missions/{id}", UUID.randomUUID()).with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null))))
                .andExpect(status().isNotFound());
    }

    @Test
    void validate_shouldReturnCorrect() throws Exception {
        var missionId = UUID.randomUUID();
        var result = new ValidationResult(true, "Correct!");
        when(validateMissionUseCase.handle(any())).thenReturn(result);

        var body = objectMapper.writeValueAsString(new MissionDto.ValidationRequest(List.of(Map.of("id", 1))));

        mockMvc.perform(post("/api/missions/{id}/validate", missionId)
                        .with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.feedback").value("Correct!"));
    }

    @Test
    void validate_shouldReturnIncorrect() throws Exception {
        var missionId = UUID.randomUUID();
        var result = new ValidationResult(false, "Wrong answer");
        when(validateMissionUseCase.handle(any())).thenReturn(result);

        var body = objectMapper.writeValueAsString(new MissionDto.ValidationRequest(List.of(Map.of("id", 2))));

        mockMvc.perform(post("/api/missions/{id}/validate", missionId)
                        .with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(false))
                .andExpect(jsonPath("$.feedback").value("Wrong answer"));
    }

    @Test
    void adminValidate_shouldReturnResult() throws Exception {
        var missionId = UUID.randomUUID();
        var result = new ValidationResult(true, "Admin approved");
        when(adminValidateMissionUseCase.handle(any())).thenReturn(result);

        var body = objectMapper.writeValueAsString(new MissionDto.ValidationRequest(List.of(Map.of("id", 1))));

        mockMvc.perform(post("/api/missions/{id}/validate/admin", missionId)
                        .with(user(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true));
    }

    @Test
    void create_shouldReturnCreatedMission() throws Exception {
        var missionId = UUID.randomUUID();
        var mission = createMission(missionId, "New Mission");
        when(manageMissionUseCase.create(any())).thenReturn(mission);

        var req = new MissionDto.UpsertMissionRequest(
                "New Mission", "Brief", "Obj", null, "DDL", null, List.of("SELECT"),
                100, true, "ASTRONOMY", DifficultyLevel.BEGINNER,
                List.of(Map.of("id", 1)), null, null, true);

        mockMvc.perform(post("/api/missions")
                        .with(user(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Mission"))
                .andExpect(jsonPath("$.theme.name").value("ASTRONOMY"));
    }

    @Test
    void create_shouldReturn400WhenValidationFails() throws Exception {
        var body = """
                {"xpReward":0,"ordered":false}
                """;

        mockMvc.perform(post("/api/missions")
                        .with(user(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturnUpdatedMission() throws Exception {
        var missionId = UUID.randomUUID();
        var mission = createMission(missionId, "Updated Mission");
        when(manageMissionUseCase.update(any())).thenReturn(mission);

        var req = new MissionDto.UpsertMissionRequest(
                "Updated Mission", "Brief", "Obj", null, "DDL", null, List.of("SELECT"),
                200, false, "CYBERSECURITY", DifficultyLevel.INTERMEDIATE,
                List.of(Map.of("id", 2)), null, null, false);

        mockMvc.perform(put("/api/missions/{id}", missionId)
                        .with(user(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Mission"));
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/missions/{id}", UUID.randomUUID()).with(user(USER_ID)))
                .andExpect(status().isNoContent());
    }

    @Test
    void findByIdAdmin_shouldReturnMissionWithExpectedResult() throws Exception {
        var missionId = UUID.randomUUID();
        var expectedResult = List.of(Map.<String, Object>of("id", 1));
        var mission = Mission.builder()
                .id(missionId).title("Admin View").briefing("Brief").objective("Obj")
                .hint(null).ddlScript("DDL").dmlScript(null)
                .techniques(List.of(new Technique(null, "SELECT"))).xpReward(100)
                .expectedResult(new ExpectedTuple(expectedResult))
                .ordered(true).theme(new Theme(UUID.randomUUID(), "ASTRONOMY", null, null)).difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(null).orderIndex(null).enabled(true).requiredLevel(1)
                .build();
        when(getAdminMissionsUseCase.findById(missionId)).thenReturn(new GetAdminMissionsUseCase.AdminMissionResult(mission, null, null));

        mockMvc.perform(get("/api/missions/{id}/admin", missionId).with(user(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Admin View"))
                .andExpect(jsonPath("$.expectedResult[0].id").value(1));
    }

    @Test
    void listAllAdmin_shouldReturnAllMissions() throws Exception {
        var mission = createMission(UUID.randomUUID(), "Admin List");
        when(getAdminMissionsUseCase.listAll()).thenReturn(List.of(new GetAdminMissionsUseCase.AdminMissionResult(mission, null, null)));

        mockMvc.perform(get("/api/missions/admin").with(user(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Admin List"));
    }
}
