package com.sqlab.application.usecase;

import com.sqlab.application.port.in.AuthenticateUserUseCase;
import com.sqlab.application.port.out.PasswordHasher;
import com.sqlab.application.port.out.UserRepository;
import com.sqlab.application.port.out.TokenProvider;
import com.sqlab.domain.exception.InvalidCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthenticateUserService implements AuthenticateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    public AuthenticateUserService(
            UserRepository userRepository, PasswordHasher passwordHasher, TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public AuthResult handle(Command command) {
        var user = userRepository.findByEmail(command.email()).orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasher.matches(command.rawPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = tokenProvider.generate(user.getId(), user.getUsername(), user.getRole());
        return new AuthResult(token, user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }
}