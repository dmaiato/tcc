package com.sqlab.application.port.in;

public interface AuthenticateUserUseCase {

    record Command(String email, String rawPassword) {}

    String handle(Command command);
}