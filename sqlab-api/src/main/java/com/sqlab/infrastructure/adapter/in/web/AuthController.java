package com.sqlab.infrastructure.adapter.in.web;

import com.sqlab.application.port.in.AuthenticateUserUseCase;
import com.sqlab.application.port.in.RegisterUserUseCase;
import com.sqlab.application.port.out.UserRepository;
import com.sqlab.domain.model.User;
import com.sqlab.infrastructure.adapter.in.web.dto.AuthDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final UserRepository userRepository;

    public AuthController(RegisterUserUseCase registerUserUseCase,
                          AuthenticateUserUseCase authenticateUserUseCase,
                          UserRepository userRepository) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthDto.AuthResponseWithUser> register(@Valid @RequestBody AuthDto.RegisterRequest request) {
        User user = registerUserUseCase.handle(
                new RegisterUserUseCase.Command(request.username(), request.email(), request.password())
        );
        String token = authenticateUserUseCase.handle(
                new AuthenticateUserUseCase.Command(request.email(), request.password())
        );
        return ResponseEntity.ok(new AuthDto.AuthResponseWithUser(token, user.getId(), user.getUsername(), user.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDto.AuthResponseWithUser> login(@Valid @RequestBody AuthDto.LoginRequest request) {
        String token = authenticateUserUseCase.handle(
                new AuthenticateUserUseCase.Command(request.email(), request.password())
        );
        var user = userRepository.findByEmail(request.email()).orElseThrow();
        return ResponseEntity.ok(new AuthDto.AuthResponseWithUser(token, user.getId(), user.getUsername(), user.getEmail()));
    }
}