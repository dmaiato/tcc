package com.sqlab.application.port.out;

import com.sqlab.domain.model.UserRole;

import java.util.UUID;

/**
 * Porta de saída responsável pela geração de tokens de autenticação.
 * Desacopla o caso de uso da implementação concreta (ex: JWT).
 */
public interface TokenProvider {
    String generate(UUID userId, String username, UserRole role);
}