package com.sqlab.application.usecase;

import com.sqlab.application.port.in.RegisterUserUseCase;
import com.sqlab.application.port.out.UserRepository;
import com.sqlab.domain.exception.UserAlreadyExistsException;
import com.sqlab.domain.model.User;
import com.sqlab.domain.model.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User handle(Command command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException("E-mail já cadastrado: " + command.email());
        }
        if (userRepository.existsByUsername(command.username())) {
            throw new UserAlreadyExistsException("Nome de usuário já em uso: " + command.username());
        }

        User user = new User(
                UUID.randomUUID(),
                command.username(),
                command.email(),
                passwordEncoder.encode(command.rawPassword()),
                0,
                UserRole.USER
        );

        return userRepository.save(user);
    }
}