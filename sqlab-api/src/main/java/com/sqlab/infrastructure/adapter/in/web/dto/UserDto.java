package com.sqlab.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class UserDto {

    public record ProfileResponse(UUID id, String username, String email, int xp, int level, String role, @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime createdAt) {}

    public record ProgressResponse(UUID missionId, boolean completed, @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime completedAt, String missionTitle, UUID scenarioId, String scenarioTitle) {}

    public record SkillsResponse(List<String> skills) {}
}