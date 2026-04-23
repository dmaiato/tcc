package com.sqlab.application.usecase;

import com.sqlab.application.port.in.AuthenticateUserUseCase;
import com.sqlab.application.port.out.UserRepository;
import com.sqlab.application.port.out.TokenProvider;
import com.sqlab.domain.exception.InvalidCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthenticateUserService implements AuthenticateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public AuthenticateUserService(
            UserRepository userRepository, PasswordEncoder passwordEncoder, TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public String handle(Command command) {
        var user = userRepository.findByEmail(command.email()).orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(command.rawPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return tokenProvider.generate(user.getId(), user.getUsername());
    }
}