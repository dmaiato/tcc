package com.sqlab.application.usecase;

import com.sqlab.application.port.in.RegisterUserUseCase;
import com.sqlab.application.port.out.PasswordHasher;
import com.sqlab.application.port.out.UserRepository;
import com.sqlab.domain.exception.UserAlreadyExistsException;
import com.sqlab.domain.model.User;
import com.sqlab.domain.model.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public RegisterUserService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    @Transactional
    public User handle(Command command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException("Email already registered: " + command.email());
        }
        if (userRepository.existsByUsername(command.username())) {
            throw new UserAlreadyExistsException("Username already in use: " + command.username());
        }

        User user = new User(
                UUID.randomUUID(),
                command.username(),
                command.email(),
                passwordHasher.encode(command.rawPassword()),
                0,
                UserRole.USER,
                LocalDateTime.now()
        );

        return userRepository.save(user);
    }
}