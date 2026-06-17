package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetProfileUseCase;
import com.sqlab.application.port.in.GetUserProgressUseCase;
import com.sqlab.application.port.in.GetUserSkillsUseCase;
import com.sqlab.application.port.out.UserRepository;
import com.sqlab.domain.exception.UserNotFoundException;
import com.sqlab.domain.model.User;
import com.sqlab.domain.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetProfileServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private GetUserProgressUseCase getUserProgressUseCase;
    @Mock
    private GetUserSkillsUseCase getUserSkillsUseCase;

    private GetProfileService service;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new GetProfileService(userRepository, getUserProgressUseCase, getUserSkillsUseCase);
    }

    @Test
    void returnsProfileWhenUserExists() {
        var user = new User(userId, "alice", "alice@test.com", "hash", 150, UserRole.USER, LocalDateTime.now());
        var progress = List.<GetUserProgressUseCase.ProgressItem>of();
        var skills = List.of("SELECT", "WHERE");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(getUserProgressUseCase.handle(new GetUserProgressUseCase.Query(userId))).thenReturn(progress);
        when(getUserSkillsUseCase.handle(new GetUserSkillsUseCase.Query(userId))).thenReturn(skills);

        var result = service.handle(new GetProfileUseCase.Query(userId));

        assertThat(result.user()).isEqualTo(user);
        assertThat(result.progress()).isSameAs(progress);
        assertThat(result.skills()).isSameAs(skills);
    }

    @Test
    void throwsWhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.handle(new GetProfileUseCase.Query(userId)))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void delegatesProgressAndSkills() {
        var user = new User(userId, "bob", "bob@test.com", "hash", 0, UserRole.USER, LocalDateTime.now());
        var progress = List.of(new GetUserProgressUseCase.ProgressItem(
                UUID.randomUUID(), true, null, "M1", null, null));
        var skills = List.of("SELECT");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(getUserProgressUseCase.handle(new GetUserProgressUseCase.Query(userId))).thenReturn(progress);
        when(getUserSkillsUseCase.handle(new GetUserSkillsUseCase.Query(userId))).thenReturn(skills);

        var result = service.handle(new GetProfileUseCase.Query(userId));

        assertThat(result.progress()).hasSize(1);
        assertThat(result.skills()).containsExactly("SELECT");
        verify(getUserProgressUseCase).handle(new GetUserProgressUseCase.Query(userId));
        verify(getUserSkillsUseCase).handle(new GetUserSkillsUseCase.Query(userId));
    }
}
