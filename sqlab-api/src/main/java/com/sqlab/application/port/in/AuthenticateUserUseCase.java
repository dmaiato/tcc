package com.sqlab.application.port.in;

import com.sqlab.domain.model.UserRole;

import java.util.UUID;

public interface AuthenticateUserUseCase {

    record Command(String email, String rawPassword) {}

    record AuthResult(String token, UUID userId, String username, String email, UserRole role) {}

    AuthResult handle(Command command);
}