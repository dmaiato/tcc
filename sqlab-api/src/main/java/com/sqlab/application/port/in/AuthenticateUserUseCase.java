package com.sqlab.application.port.in;

import com.sqlab.domain.model.UserRole;

public interface AuthenticateUserUseCase {

    record Command(String email, String rawPassword) {}

    record AuthResult(String token, UserRole role) {}

    AuthResult handle(Command command);
}