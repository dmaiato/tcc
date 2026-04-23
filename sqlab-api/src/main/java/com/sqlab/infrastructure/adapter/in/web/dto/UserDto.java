package com.sqlab.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class UserDto {

    public record ProfileResponse(UUID id, String username, String email, int xp) {}

    public record ProgressResponse(UUID missionId, boolean completed, LocalDateTime completedAt) {}

    public record SkillsResponse(List<String> skills) {}
}