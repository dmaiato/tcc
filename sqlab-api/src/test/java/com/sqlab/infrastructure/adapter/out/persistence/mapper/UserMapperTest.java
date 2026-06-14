package com.sqlab.infrastructure.adapter.out.persistence.mapper;

import com.sqlab.domain.model.User;
import com.sqlab.domain.model.UserRole;
import com.sqlab.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();
    private final UUID id = UUID.randomUUID();
    private final LocalDateTime now = LocalDateTime.now();

    @Test
    void toDomain() {
        var entity = UserJpaEntity.builder()
                .id(id)
                .username("alice")
                .email("alice@example.com")
                .passwordHash("hash123")
                .xp(500)
                .role("USER")
                .createdAt(now)
                .build();

        var domain = mapper.toDomain(entity);

        assertEquals(id, domain.getId());
        assertEquals("alice", domain.getUsername());
        assertEquals("alice@example.com", domain.getEmail());
        assertEquals("hash123", domain.getPasswordHash());
        assertEquals(500, domain.getXp());
        assertEquals(UserRole.USER, domain.getRole());
        assertEquals(now, domain.getCreatedAt());
    }

    @Test
    void toDomainAdmin() {
        var entity = UserJpaEntity.builder()
                .id(id)
                .username("admin")
                .email("admin@example.com")
                .passwordHash("adminhash")
                .xp(0)
                .role("ADMIN")
                .createdAt(now)
                .build();

        var domain = mapper.toDomain(entity);

        assertEquals(UserRole.ADMIN, domain.getRole());
    }

    @Test
    void toJpa() {
        var domain = new User(id, "bob", "bob@example.com", "$2a$10...", 300, UserRole.USER, now);

        var entity = mapper.toJpa(domain);

        assertEquals(id, entity.getId());
        assertEquals("bob", entity.getUsername());
        assertEquals("bob@example.com", entity.getEmail());
        assertEquals("$2a$10...", entity.getPasswordHash());
        assertEquals(300, entity.getXp());
        assertEquals("USER", entity.getRole());
        assertEquals(now, entity.getCreatedAt());
    }
}
