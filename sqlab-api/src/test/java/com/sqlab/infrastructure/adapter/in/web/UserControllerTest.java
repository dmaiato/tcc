package com.sqlab.infrastructure.adapter.in.web;

import com.sqlab.application.port.in.GetUserProgressUseCase;
import com.sqlab.application.port.in.GetUserSkillsUseCase;
import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.application.port.out.UserRepository;
import com.sqlab.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    private static final String USER_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetUserProgressUseCase getUserProgressUseCase;

    @MockitoBean
    private GetUserSkillsUseCase getUserSkillsUseCase;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private MissionRepository missionRepository;

    @MockitoBean
    private ScenarioRepository scenarioRepository;

    @Test
    void getProfile_shouldReturnProfile() throws Exception {
        var id = UUID.fromString(USER_ID);
        var user = new User(id, "alice", "alice@test.com", "hash", 250, UserRole.USER, LocalDateTime.of(2025, 1, 1, 0, 0));
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/me").with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@test.com"))
                .andExpect(jsonPath("$.xp").value(250))
                .andExpect(jsonPath("$.level").value(2))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void getProfile_shouldReturn404WhenUserNotFound() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/me").with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProgress_shouldReturnProgressList() throws Exception {
        var userId = UUID.fromString(USER_ID);
        var missionId = UUID.randomUUID();
        var scenarioId = UUID.randomUUID();
        var completedAt = LocalDateTime.of(2025, 6, 1, 10, 30);

        when(getUserProgressUseCase.handle(any())).thenReturn(List.of(
                new Progress(UUID.randomUUID(), userId, missionId, true, completedAt)
        ));
        var mission = Mission.builder()
                .id(missionId).title("Mission 1").briefing("Brief").objective("Obj")
                .hint(null).ddlScript("DDL").dmlScript(null)
                .techniques(List.of()).xpReward(100)
                .expectedResult(new ExpectedTuple(List.of()))
                .ordered(true).theme(Theme.ASTRONOMY).difficulty(DifficultyLevel.BEGINNER)
                .scenarioId(scenarioId).orderIndex(null).enabled(true).requiredLevel(1)
                .build();
        when(missionRepository.findAllById(Set.of(missionId))).thenReturn(List.of(mission));
        when(scenarioRepository.findAllById(Set.of(scenarioId))).thenReturn(
                List.of(new Scenario(scenarioId, "Scenario 1", "Desc", Theme.ASTRONOMY, true, 1)));

        mockMvc.perform(get("/api/users/me/progress").with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].missionId").value(missionId.toString()))
                .andExpect(jsonPath("$[0].completed").value(true))
                .andExpect(jsonPath("$[0].missionTitle").value("Mission 1"))
                .andExpect(jsonPath("$[0].scenarioTitle").value("Scenario 1"));
    }

    @Test
    void getProgress_shouldReturnEmptyListWhenNoProgress() throws Exception {
        when(getUserProgressUseCase.handle(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/users/me/progress").with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getSkills_shouldReturnSkills() throws Exception {
        when(getUserSkillsUseCase.handle(any())).thenReturn(List.of("SELECT", "JOIN"));

        mockMvc.perform(get("/api/users/me/skills").with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skills[0]").value("SELECT"))
                .andExpect(jsonPath("$.skills[1]").value("JOIN"));
    }

    @Test
    void getSkills_shouldReturnEmptyListWhenNoSkills() throws Exception {
        when(getUserSkillsUseCase.handle(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/users/me/skills").with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skills").isEmpty());
    }
}
