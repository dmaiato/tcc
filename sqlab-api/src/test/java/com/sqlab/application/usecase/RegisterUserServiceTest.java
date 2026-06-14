package com.sqlab.application.usecase;

import com.sqlab.application.port.in.RegisterUserUseCase;
import com.sqlab.application.port.out.UserRepository;
import com.sqlab.domain.exception.UserAlreadyExistsException;
import com.sqlab.domain.model.User;
import com.sqlab.domain.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private RegisterUserService service;

    @BeforeEach
    void setUp() {
        service = new RegisterUserService(userRepository, passwordEncoder);
    }

    @Test
    void successfulRegistration() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("$2a$10encoded");
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var command = new RegisterUserUseCase.Command("alice", "alice@example.com", "secret");
        var result = service.handle(command);

        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        assertThat(result.getPasswordHash()).isEqualTo("$2a$10encoded");
        assertThat(result.getXp()).isZero();
        assertThat(result.getRole()).isEqualTo(UserRole.USER);
        assertThat(result.getId()).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();
        verify(userRepository).save(any());
    }

    @Test
    void throwsWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);
        var command = new RegisterUserUseCase.Command("alice", "alice@example.com", "secret");
        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("E-mail");
    }

    @Test
    void throwsWhenUsernameAlreadyExists() {
        when(userRepository.existsByEmail("bob@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("bob")).thenReturn(true);
        var command = new RegisterUserUseCase.Command("bob", "bob@example.com", "secret");
        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("usuário");
    }

    @Test
    void passwordIsEncoded() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode("raw")).thenReturn("bcrypt-result");
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.handle(new RegisterUserUseCase.Command("u", "u@e", "raw"));
        assertThat(result.getPasswordHash()).isEqualTo("bcrypt-result");
        verify(passwordEncoder).encode("raw");
    }
}
