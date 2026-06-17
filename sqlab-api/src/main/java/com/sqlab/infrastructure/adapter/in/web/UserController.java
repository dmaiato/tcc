package com.sqlab.infrastructure.adapter.in.web;

import com.sqlab.application.port.in.GetProfileUseCase;
import com.sqlab.application.port.in.GetUserProgressUseCase;
import com.sqlab.application.port.in.GetUserSkillsUseCase;
import com.sqlab.infrastructure.adapter.in.web.dto.UserDto;
import com.sqlab.infrastructure.adapter.in.web.dto.mapper.UserDtoMapper;
import com.sqlab.infrastructure.adapter.in.web.util.ControllerUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final GetProfileUseCase getProfileUseCase;
    private final GetUserProgressUseCase getUserProgressUseCase;
    private final GetUserSkillsUseCase getUserSkillsUseCase;

    public UserController(GetProfileUseCase getProfileUseCase,
                          GetUserProgressUseCase getUserProgressUseCase,
                          GetUserSkillsUseCase getUserSkillsUseCase) {
        this.getProfileUseCase = getProfileUseCase;
        this.getUserProgressUseCase = getUserProgressUseCase;
        this.getUserSkillsUseCase = getUserSkillsUseCase;
    }

    @GetMapping
    public ResponseEntity<UserDto.ProfileResponse> getProfile(@AuthenticationPrincipal String userId) {
        var profile = getProfileUseCase.handle(new GetProfileUseCase.Query(ControllerUtils.parseUserId(userId)));
        return ResponseEntity.ok(UserDtoMapper.toProfileResponse(profile.user()));
    }

    @GetMapping("/progress")
    public ResponseEntity<List<UserDto.ProgressResponse>> getProgress(@AuthenticationPrincipal String userId) {
        List<GetUserProgressUseCase.ProgressItem> items = getUserProgressUseCase
                .handle(new GetUserProgressUseCase.Query(ControllerUtils.parseUserId(userId)));
        return ResponseEntity.ok(UserDtoMapper.toProgressResponseList(items));
    }

    @GetMapping("/skills")
    public ResponseEntity<UserDto.SkillsResponse> getSkills(@AuthenticationPrincipal String userId) {
        List<String> skills = getUserSkillsUseCase.handle(new GetUserSkillsUseCase.Query(ControllerUtils.parseUserId(userId)));
        return ResponseEntity.ok(UserDtoMapper.toSkillsResponse(skills));
    }

}
