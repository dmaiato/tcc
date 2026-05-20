package com.sqlab.infrastructure.adapter.in.web;

import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class AdminValidateMissionIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private MissionRepository missionRepository;

    private UUID missionId;
    private Mission mission;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        missionId = UUID.randomUUID();
        ExpectedTuple expected = new ExpectedTuple(List.of(
                Map.of("name", (Object) "Alice")
        ));

        mission = Mission.builder()
                .id(missionId)
                .title("Test")
                .briefing("Briefing")
                .objective("Objective")
                .ddlScript("CREATE TABLE t (name TEXT)")
                .dmlScript("INSERT INTO t VALUES ('Alice')")
                .techniques(List.of("SELECT"))
                .xpReward(100)
                .expectedResult(expected)
                .theme(Theme.CRIMINAL)
                .difficulty(DifficultyLevel.BEGINNER)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnCorrectWhenAdminAndValidSolution() throws Exception {
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));

        String requestBody = """
                {"tuples": [{"name": "Alice"}]}
                """;

        mockMvc.perform(post("/api/missions/{id}/validate/admin", missionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnIncorrectWhenAdminAndWrongSolution() throws Exception {
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));

        String requestBody = """
                {"tuples": [{"name": "Bob"}]}
                """;

        mockMvc.perform(post("/api/missions/{id}/validate/admin", missionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenNotAdmin() throws Exception {
        mockMvc.perform(post("/api/missions/{id}/validate/admin", missionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tuples\": []}"))
                .andExpect(status().isForbidden());
    }
}
