package com.sqlab.infrastructure.adapter.in.web;

import com.sqlab.application.port.in.GetUserProgressUseCase;
import com.sqlab.application.port.in.GetUserSkillsUseCase;
import com.sqlab.application.port.out.UserRepository;
import com.sqlab.infrastructure.adapter.in.web.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final GetUserProgressUseCase getUserProgressUseCase;
    private final GetUserSkillsUseCase getUserSkillsUseCase;
    private final UserRepository userRepository;

    public UserController(GetUserProgressUseCase getUserProgressUseCase,
                          GetUserSkillsUseCase getUserSkillsUseCase,
                          UserRepository userRepository) {
        this.getUserProgressUseCase = getUserProgressUseCase;
        this.getUserSkillsUseCase = getUserSkillsUseCase;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<UserDto.ProfileResponse> getProfile(@AuthenticationPrincipal String userId) {
        return userRepository.findById(UUID.fromString(userId))
                .map(u -> ResponseEntity.ok(new UserDto.ProfileResponse(u.getId(), u.getUsername(), u.getEmail(), u.getXp())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/progress")
    public ResponseEntity<List<UserDto.ProgressResponse>> getProgress(@AuthenticationPrincipal String userId) {
        List<UserDto.ProgressResponse> response = getUserProgressUseCase
                .handle(new GetUserProgressUseCase.Query(UUID.fromString(userId)))
                .stream()
                .map(p -> new UserDto.ProgressResponse(p.getMissionId(), p.isCompleted(), p.getCompletedAt()))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/skills")
    public ResponseEntity<UserDto.SkillsResponse> getSkills(@AuthenticationPrincipal String userId) {
        List<String> skills = getUserSkillsUseCase.handle(new GetUserSkillsUseCase.Query(UUID.fromString(userId)));
        return ResponseEntity.ok(new UserDto.SkillsResponse(skills));
    }
}