package com.sqlab.infrastructure.adapter.in.web.dto.mapper;

import com.sqlab.application.port.in.GetUserProgressUseCase;
import com.sqlab.domain.model.User;
import com.sqlab.infrastructure.adapter.in.web.dto.UserDto;

import java.util.List;

public class UserDtoMapper {

    public static UserDto.ProfileResponse toProfileResponse(User user) {
        return new UserDto.ProfileResponse(
                user.getId(), user.getUsername(), user.getEmail(),
                user.getXp(), User.computeLevel(user.getXp()),
                user.getRole().name(), user.getCreatedAt());
    }

    public static UserDto.ProgressResponse toProgressResponse(GetUserProgressUseCase.ProgressItem item) {
        return new UserDto.ProgressResponse(
                item.missionId(), item.completed(), item.completedAt(),
                item.missionTitle(), item.scenarioId(), item.scenarioTitle());
    }

    public static List<UserDto.ProgressResponse> toProgressResponseList(
            List<GetUserProgressUseCase.ProgressItem> items) {
        return items.stream().map(UserDtoMapper::toProgressResponse).toList();
    }

    public static UserDto.SkillsResponse toSkillsResponse(List<String> skills) {
        return new UserDto.SkillsResponse(skills);
    }

    private UserDtoMapper() {
    }
}
