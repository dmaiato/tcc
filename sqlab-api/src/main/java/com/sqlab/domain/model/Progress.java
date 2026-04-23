package com.sqlab.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class Progress {

    private final UUID id;
    private final UUID userId;
    private final UUID missionId;
    private final boolean completed;
    private final LocalDateTime completedAt;

    public static Progress complete(UUID userId, UUID missionId) {
        return new Progress(UUID.randomUUID(), userId, missionId, true, LocalDateTime.now());
    }
}