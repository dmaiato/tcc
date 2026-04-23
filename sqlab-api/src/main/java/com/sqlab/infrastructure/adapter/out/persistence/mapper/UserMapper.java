package com.sqlab.infrastructure.adapter.out.persistence.mapper;

import com.sqlab.domain.model.User;
import com.sqlab.domain.model.UserRole;
import com.sqlab.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toDomain(UserJpaEntity entity) {
        return new User(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getXp(),
                UserRole.valueOf(entity.getRole())
        );
    }

    public UserJpaEntity toJpa(User user) {
        return UserJpaEntity.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .xp(user.getXp())
                .role(user.getRole().name())
                .build();
    }
}