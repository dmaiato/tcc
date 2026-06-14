package com.sqlab.application.usecase;

import com.sqlab.application.port.in.AuthenticateUserUseCase;
import com.sqlab.application.port.out.TokenProvider;
import com.sqlab.application.port.out.UserRepository;
import com.sqlab.domain.exception.InvalidCredentialsException;
import com.sqlab.domain.model.User;
import com.sqlab.domain.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenProvider tokenProvider;

    private AuthenticateUserService service;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new AuthenticateUserService(userRepository, passwordEncoder, tokenProvider);
    }

    @Test
    void successfulAuthenticationReturnsTokenAndRole() {
        var user = new User(userId, "alice", "alice@example.com", "hash", 0, UserRole.USER, LocalDateTime.now());
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hash")).thenReturn(true);
        when(tokenProvider.generate(userId, "alice", UserRole.USER)).thenReturn("jwt-token");

        var result = service.handle(new AuthenticateUserUseCase.Command("alice@example.com", "password123"));

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.role()).isEqualTo(UserRole.USER);
    }

    @Test
    void throwsWhenEmailNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.handle(new AuthenticateUserUseCase.Command("unknown@example.com", "pwd")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void throwsWhenPasswordIncorrect() {
        var user = new User(userId, "alice", "alice@example.com", "hash", 0, UserRole.USER, LocalDateTime.now());
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.handle(new AuthenticateUserUseCase.Command("alice@example.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void usesSameExceptionForUnknownEmailAndWrongPassword() {
        when(userRepository.findByEmail("x@y")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.handle(new AuthenticateUserUseCase.Command("x@y", "pwd")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Credenciais inválidas.");
    }
}
