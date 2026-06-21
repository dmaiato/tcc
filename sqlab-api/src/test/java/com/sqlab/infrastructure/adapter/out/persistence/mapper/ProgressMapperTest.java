package com.sqlab.infrastructure.adapter.out.persistence.mapper;

import com.sqlab.domain.model.Progress;
import com.sqlab.domain.model.UserRole;
import com.sqlab.infrastructure.adapter.out.persistence.entity.MissionJpaEntity;
import com.sqlab.infrastructure.adapter.out.persistence.entity.ProgressJpaEntity;
import com.sqlab.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProgressMapperTest {

    private final ProgressMapper mapper = new ProgressMapper();
    private final UUID userId = UUID.randomUUID();
    private final UUID missionId = UUID.randomUUID();
    private final LocalDateTime now = LocalDateTime.now();

    private UserJpaEntity userJpa() {
        return UserJpaEntity.builder().id(userId).username("u").email("u@e").passwordHash("h")
                .xp(0).role(UserRole.USER).createdAt(now).build();
    }

    private MissionJpaEntity missionJpa() {
        return MissionJpaEntity.builder()
                .id(missionId).title("M").briefing("B").objective("O")
                .ddlScript("DDL").xpReward(10)
                .expectedResult(List.of(Map.of("x", 1)))
                .ordered(false).difficulty(com.sqlab.domain.model.DifficultyLevel.BEGINNER)
                .enabled(true).createdAt(now)
                .build();
    }

    @Test
    void toDomainCompleted() {
        var entity = ProgressJpaEntity.builder()
                .id(UUID.randomUUID())
                .user(userJpa())
                .mission(missionJpa())
                .completed(true)
                .completedAt(now)
                .createdAt(now)
                .build();

        var domain = mapper.toDomain(entity);

        assertEquals(userId, domain.getUserId());
        assertEquals(missionId, domain.getMissionId());
        assertTrue(domain.isCompleted());
        assertEquals(now, domain.getCompletedAt());
    }

    @Test
    void toDomainNotCompleted() {
        var entity = ProgressJpaEntity.builder()
                .id(UUID.randomUUID())
                .user(userJpa())
                .mission(missionJpa())
                .completed(false)
                .completedAt(null)
                .createdAt(now)
                .build();

        var domain = mapper.toDomain(entity);

        assertFalse(domain.isCompleted());
        assertNull(domain.getCompletedAt());
    }

    @Test
    void toJpa() {
        var domain = Progress.complete(userId, missionId);
        var userJpa = userJpa();
        var missionJpa = missionJpa();

        var entity = mapper.toJpa(domain, userJpa, missionJpa);

        assertEquals(domain.getId(), entity.getId());
        assertSame(userJpa, entity.getUser());
        assertSame(missionJpa, entity.getMission());
        assertTrue(entity.isCompleted());
        assertNotNull(entity.getCompletedAt());
    }
}
