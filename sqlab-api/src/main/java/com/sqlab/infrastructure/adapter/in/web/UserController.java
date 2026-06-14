package com.sqlab.infrastructure.adapter.in.web;

import com.sqlab.application.port.in.GetUserProgressUseCase;
import com.sqlab.application.port.in.GetUserSkillsUseCase;
import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.application.port.out.UserRepository;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Progress;
import com.sqlab.domain.model.User;
import com.sqlab.infrastructure.adapter.in.web.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final GetUserProgressUseCase getUserProgressUseCase;
    private final GetUserSkillsUseCase getUserSkillsUseCase;
    private final UserRepository userRepository;
    private final MissionRepository missionRepository;
    private final ScenarioRepository scenarioRepository;

    public UserController(GetUserProgressUseCase getUserProgressUseCase,
                          GetUserSkillsUseCase getUserSkillsUseCase,
                          UserRepository userRepository,
                          MissionRepository missionRepository,
                          ScenarioRepository scenarioRepository) {
        this.getUserProgressUseCase = getUserProgressUseCase;
        this.getUserSkillsUseCase = getUserSkillsUseCase;
        this.userRepository = userRepository;
        this.missionRepository = missionRepository;
        this.scenarioRepository = scenarioRepository;
    }

    @GetMapping
    public ResponseEntity<UserDto.ProfileResponse> getProfile(@AuthenticationPrincipal String userId) {
        return userRepository.findById(parseUserId(userId))
                .map(u -> ResponseEntity.ok(new UserDto.ProfileResponse(u.getId(), u.getUsername(), u.getEmail(), u.getXp(), User.computeLevel(u.getXp()), u.getRole().name(), u.getCreatedAt())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/progress")
    public ResponseEntity<List<UserDto.ProgressResponse>> getProgress(@AuthenticationPrincipal String userId) {
        List<Progress> progressList = getUserProgressUseCase
                .handle(new GetUserProgressUseCase.Query(parseUserId(userId)));

        if (progressList.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        Set<UUID> missionIds = progressList.stream()
                .map(Progress::getMissionId)
                .collect(Collectors.toSet());
        Map<UUID, Mission> missionMap = new HashMap<>();
        missionRepository.findAllById(missionIds)
                .forEach(m -> missionMap.put(m.getId(), m));

        Set<UUID> scenarioIds = missionMap.values().stream()
                .map(Mission::getScenarioId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, String> scenarioTitles = new HashMap<>();
        scenarioRepository.findAllById(scenarioIds)
                .forEach(s -> scenarioTitles.put(s.getId(), s.getTitle()));

        List<UserDto.ProgressResponse> response = progressList.stream()
                .map(p -> {
                    Mission m = missionMap.get(p.getMissionId());
                    return new UserDto.ProgressResponse(
                            p.getMissionId(), p.isCompleted(), p.getCompletedAt(),
                            m != null ? m.getTitle() : null,
                            m != null ? m.getScenarioId() : null,
                            m != null ? scenarioTitles.get(m.getScenarioId()) : null);
                })
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/skills")
    public ResponseEntity<UserDto.SkillsResponse> getSkills(@AuthenticationPrincipal String userId) {
        List<String> skills = getUserSkillsUseCase.handle(new GetUserSkillsUseCase.Query(parseUserId(userId)));
        return ResponseEntity.ok(new UserDto.SkillsResponse(skills));
    }

    private UUID parseUserId(String userId) {
        if (userId == null) return null;
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
