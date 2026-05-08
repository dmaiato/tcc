package com.sqlab.infrastructure.adapter.in.web;

import com.sqlab.application.port.in.GetMissionsUseCase;
import com.sqlab.application.port.in.ValidateMissionUseCase;
import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Theme;
import com.sqlab.infrastructure.adapter.in.web.dto.MissionDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private final GetMissionsUseCase getMissionsUseCase;
    private final ValidateMissionUseCase validateMissionUseCase;

    public MissionController(GetMissionsUseCase getMissionsUseCase,
                             ValidateMissionUseCase validateMissionUseCase) {
        this.getMissionsUseCase = getMissionsUseCase;
        this.validateMissionUseCase = validateMissionUseCase;
    }

    @GetMapping
    public ResponseEntity<List<MissionDto.MissionSummary>> listAll(
            @RequestParam(required = false) Theme theme,
            @RequestParam(required = false) DifficultyLevel difficulty) {

        List<MissionDto.MissionSummary> response = getMissionsUseCase
                .handle(new GetMissionsUseCase.ListAllQuery(theme, difficulty))
                .stream()
                .map(this::toSummary)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{missionId}")
    public ResponseEntity<MissionDto.MissionResponse> findById(@PathVariable UUID missionId) {
        Mission mission = getMissionsUseCase.handle(new GetMissionsUseCase.FindByIdQuery(missionId));
        return ResponseEntity.ok(toResponse(mission));
    }

    @PostMapping("/{missionId}/validate")
    public ResponseEntity<MissionDto.ValidationResponse> validate(
            @PathVariable UUID missionId,
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody MissionDto.ValidationRequest request) {

        boolean correct = validateMissionUseCase.handle(
                new ValidateMissionUseCase.Command(UUID.fromString(userId), missionId, request.tuples())
        );
        return ResponseEntity.ok(new MissionDto.ValidationResponse(correct));
    }

    private MissionDto.MissionSummary toSummary(Mission m) {
        return new MissionDto.MissionSummary(
                m.getId(), m.getTitle(), m.getTechniques(),
                m.getXpReward(), m.isOrdered(), m.getTheme(), m.getDifficulty());
    }

    private MissionDto.MissionResponse toResponse(Mission m) {
        return new MissionDto.MissionResponse(
                m.getId(), m.getTitle(), m.getBriefing(),
                m.getObjective(), m.getHint(),
                m.getDdlScript(), m.getDmlScript(), m.getTechniques(),
                m.getXpReward(), m.isOrdered(), m.getTheme(), m.getDifficulty());
    }
}