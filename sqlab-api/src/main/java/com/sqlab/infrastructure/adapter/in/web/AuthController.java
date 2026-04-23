package com.sqlab.infrastructure.adapter.in.web;

import com.sqlab.application.port.in.AuthenticateUserUseCase;
import com.sqlab.application.port.in.RegisterUserUseCase;
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

    public AuthController(RegisterUserUseCase registerUserUseCase,
                          AuthenticateUserUseCase authenticateUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthDto.UserResponse> register(@Valid @RequestBody AuthDto.RegisterRequest request) {
        User user = registerUserUseCase.handle(
                new RegisterUserUseCase.Command(request.username(), request.email(), request.password())
        );
        return ResponseEntity.ok(new AuthDto.UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getXp()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDto.TokenResponse> login(@Valid @RequestBody AuthDto.LoginRequest request) {
        String token = authenticateUserUseCase.handle(
                new AuthenticateUserUseCase.Command(request.email(), request.password())
        );
        return ResponseEntity.ok(new AuthDto.TokenResponse(token));
    }
}