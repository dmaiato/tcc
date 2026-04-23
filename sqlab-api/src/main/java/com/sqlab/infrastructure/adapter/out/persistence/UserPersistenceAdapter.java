package com.sqlab.infrastructure.adapter.out.persistence;

import com.sqlab.application.port.out.UserRepository;
import com.sqlab.domain.model.User;
import com.sqlab.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import com.sqlab.infrastructure.adapter.out.persistence.mapper.UserMapper;
import com.sqlab.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@Transactional
public class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserMapper mapper;

    public UserPersistenceAdapter(UserJpaRepository jpaRepository, UserMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = jpaRepository.findById(user.getId())
                .map(existing -> {
                    existing.setUsername(user.getUsername());
                    existing.setEmail(user.getEmail());
                    existing.setPasswordHash(user.getPasswordHash());
                    existing.setXp(user.getXp());
                    existing.setRole(user.getRole().name());
                    return existing;
                })
                .orElseGet(() -> mapper.toJpa(user));
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }
}