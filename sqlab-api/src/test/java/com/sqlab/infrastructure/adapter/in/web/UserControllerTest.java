package com.sqlab.infrastructure.adapter.in.web;

import com.sqlab.application.port.in.GetProfileUseCase;
import com.sqlab.application.port.in.GetUserProgressUseCase;
import com.sqlab.application.port.in.GetUserSkillsUseCase;
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
    private GetProfileUseCase getProfileUseCase;

    @MockitoBean
    private GetUserProgressUseCase getUserProgressUseCase;

    @MockitoBean
    private GetUserSkillsUseCase getUserSkillsUseCase;

    @Test
    void getProfile_shouldReturnProfile() throws Exception {
        var id = UUID.fromString(USER_ID);
        var user = new User(id, "alice", "alice@test.com", "hash", 250, UserRole.USER, LocalDateTime.of(2025, 1, 1, 0, 0));
        when(getProfileUseCase.handle(any())).thenReturn(
                new GetProfileUseCase.ProfileResponse(user, List.of(), List.of()));

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
        when(getProfileUseCase.handle(any())).thenThrow(new com.sqlab.domain.exception.UserNotFoundException(UUID.fromString(USER_ID)));

        mockMvc.perform(get("/api/users/me").with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProgress_shouldReturnProgressList() throws Exception {
        var missionId = UUID.randomUUID();
        var scenarioId = UUID.randomUUID();
        var completedAt = LocalDateTime.of(2025, 6, 1, 10, 30);

        when(getUserProgressUseCase.handle(any())).thenReturn(List.of(
                new GetUserProgressUseCase.ProgressItem(missionId, true, completedAt, "Mission 1", scenarioId, "Scenario 1")
        ));

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
