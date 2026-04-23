package com.sqlab.application.port.in;

import com.sqlab.domain.model.User;

public interface RegisterUserUseCase {

    record Command(String username, String email, String rawPassword) {}

    User handle(Command command);
}