package com.sqlab.infrastructure.adapter.in.web;

import tools.jackson.databind.ObjectMapper;
import com.sqlab.application.port.in.AuthenticateUserUseCase;
import com.sqlab.application.port.in.RegisterUserUseCase;
import com.sqlab.domain.exception.InvalidCredentialsException;
import com.sqlab.domain.exception.UserAlreadyExistsException;
import com.sqlab.domain.model.User;
import com.sqlab.domain.model.UserRole;
import com.sqlab.infrastructure.adapter.in.web.dto.AuthDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;

    @MockitoBean
    private AuthenticateUserUseCase authenticateUserUseCase;

    @Test
    void register_shouldReturnTokenAndUser() throws Exception {
        var userId = UUID.randomUUID();
        var user = new User(userId, "alice", "alice@test.com", "hash", 0, UserRole.USER, LocalDateTime.now());
        var authResult = new AuthenticateUserUseCase.AuthResult("jwt-token", UserRole.USER);

        when(registerUserUseCase.handle(any())).thenReturn(user);
        when(authenticateUserUseCase.handle(any())).thenReturn(authResult);

        var body = objectMapper.writeValueAsString(new AuthDto.RegisterRequest("alice", "alice@test.com", "pass123"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void register_shouldReturn400WhenValidationFails() throws Exception {
        var body = objectMapper.writeValueAsString(new AuthDto.RegisterRequest("", "bad", ""));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn409WhenUserAlreadyExists() throws Exception {
        when(registerUserUseCase.handle(any())).thenThrow(new UserAlreadyExistsException("Username already taken"));

        var body = objectMapper.writeValueAsString(new AuthDto.RegisterRequest("alice", "alice@test.com", "pass123"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username already taken"));
    }

    @Test
    void login_shouldReturnTokenOnSuccess() throws Exception {
        var authResult = new AuthenticateUserUseCase.AuthResult("login-token", UserRole.ADMIN);
        when(authenticateUserUseCase.handle(any())).thenReturn(authResult);

        var body = objectMapper.writeValueAsString(new AuthDto.LoginRequest("admin@test.com", "admin123"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("login-token"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void login_shouldReturn401WhenInvalidCredentials() throws Exception {
        when(authenticateUserUseCase.handle(any())).thenThrow(new InvalidCredentialsException());

        var body = objectMapper.writeValueAsString(new AuthDto.LoginRequest("bad@test.com", "wrong"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciais inválidas."));
    }

    @Test
    void login_shouldReturn400WhenValidationFails() throws Exception {
        var body = objectMapper.writeValueAsString(new AuthDto.LoginRequest("", ""));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
