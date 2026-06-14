package com.sqlab.infrastructure.adapter.in.web;

import com.sqlab.application.port.in.AuthenticateUserUseCase;
import com.sqlab.application.port.in.RegisterUserUseCase;
import com.sqlab.domain.exception.InvalidCredentialsException;
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
    public ResponseEntity<AuthDto.AuthResponseWithUser> register(@Valid @RequestBody AuthDto.RegisterRequest request) {
        User user = registerUserUseCase.handle(
                new RegisterUserUseCase.Command(request.username(), request.email(), request.password())
        );
        AuthenticateUserUseCase.AuthResult auth = authenticateUserUseCase.handle(
                new AuthenticateUserUseCase.Command(request.email(), request.password())
        );
        return ResponseEntity.ok(new AuthDto.AuthResponseWithUser(auth.token(), user.getId(), user.getUsername(), user.getEmail(), auth.role().name()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDto.AuthResponseWithUser> login(@Valid @RequestBody AuthDto.LoginRequest request) {
        AuthenticateUserUseCase.AuthResult auth = authenticateUserUseCase.handle(
                new AuthenticateUserUseCase.Command(request.email(), request.password())
        );
        return ResponseEntity.ok(new AuthDto.AuthResponseWithUser(auth.token(), null, null, null, auth.role().name()));
    }
}
