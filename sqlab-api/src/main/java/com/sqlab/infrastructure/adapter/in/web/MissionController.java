package com.sqlab.infrastructure.adapter.in.web;

import com.sqlab.application.port.in.GetMissionsUseCase;
import com.sqlab.application.port.in.AdminValidateMissionUseCase;
import com.sqlab.application.port.in.ManageMissionUseCase;
import com.sqlab.application.port.in.ValidateMissionUseCase;
import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Theme;
import com.sqlab.domain.model.ValidationResult;
import com.sqlab.infrastructure.adapter.in.web.dto.MissionDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private final GetMissionsUseCase getMissionsUseCase;
    private final ValidateMissionUseCase validateMissionUseCase;
    private final ManageMissionUseCase manageMissionUseCase;
    private final AdminValidateMissionUseCase adminValidateMissionUseCase;

    public MissionController(GetMissionsUseCase getMissionsUseCase,
                             ValidateMissionUseCase validateMissionUseCase,
                             ManageMissionUseCase manageMissionUseCase,
                             AdminValidateMissionUseCase adminValidateMissionUseCase) {
        this.getMissionsUseCase = getMissionsUseCase;
        this.validateMissionUseCase = validateMissionUseCase;
        this.manageMissionUseCase = manageMissionUseCase;
        this.adminValidateMissionUseCase = adminValidateMissionUseCase;
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
    public ResponseEntity<MissionDto.MissionResponse> findById(
            @PathVariable UUID missionId,
            @AuthenticationPrincipal String userId) {
        UUID userUuid = userId != null ? UUID.fromString(userId) : null;
        GetMissionsUseCase.MissionDetail detail = getMissionsUseCase.handleDetail(
                new GetMissionsUseCase.FindByIdQuery(missionId, userUuid));
        return ResponseEntity.ok(toResponse(detail));
    }

    @PostMapping("/{missionId}/validate")
    public ResponseEntity<MissionDto.ValidationResponse> validate(
            @PathVariable UUID missionId,
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody MissionDto.ValidationRequest request) {

        ValidationResult result = validateMissionUseCase.handle(
                new ValidateMissionUseCase.Command(UUID.fromString(userId), missionId, request.tuples())
        );
        return ResponseEntity.ok(new MissionDto.ValidationResponse(result.correct(), result.feedback()));
    }

    @PostMapping("/{missionId}/validate/admin")
    public ResponseEntity<MissionDto.ValidationResponse> adminValidate(
            @PathVariable UUID missionId,
            @Valid @RequestBody MissionDto.ValidationRequest request) {

        ValidationResult result = adminValidateMissionUseCase.handle(
                new AdminValidateMissionUseCase.Command(missionId, request.tuples())
        );
        return ResponseEntity.ok(new MissionDto.ValidationResponse(result.correct(), result.feedback()));
    }

    @PostMapping
    public ResponseEntity<MissionDto.MissionResponse> create(
            @Valid @RequestBody MissionDto.CreateMissionRequest request) {
        ManageMissionUseCase.CreateMissionCommand command = new ManageMissionUseCase.CreateMissionCommand(
                request.title(), request.briefing(), request.objective(),
                request.hint(), request.ddlScript(), request.dmlScript(),
                request.techniques(), request.xpReward(), request.ordered(),
                request.theme(), request.difficulty(), request.expectedResult(),
                request.scenarioId(), request.orderIndex(),
                request.enabled());
        Mission mission = manageMissionUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toMissionResponse(mission));
    }

    @PutMapping("/{missionId}")
    public ResponseEntity<MissionDto.MissionResponse> update(
            @PathVariable UUID missionId,
            @Valid @RequestBody MissionDto.UpdateMissionRequest request) {
        ManageMissionUseCase.UpdateMissionCommand command = new ManageMissionUseCase.UpdateMissionCommand(
                missionId,
                request.title(), request.briefing(), request.objective(),
                request.hint(), request.ddlScript(), request.dmlScript(),
                request.techniques(), request.xpReward(), request.ordered(),
                request.theme(), request.difficulty(), request.expectedResult(),
                request.scenarioId(), request.orderIndex(),
                request.enabled());
        Mission mission = manageMissionUseCase.update(command);
        return ResponseEntity.ok(toMissionResponse(mission));
    }

    @GetMapping("/{missionId}/admin")
    public ResponseEntity<MissionDto.MissionResponse> findByIdAdmin(@PathVariable UUID missionId) {
        Mission mission = manageMissionUseCase.findById(missionId);
        return ResponseEntity.ok(toMissionResponse(mission));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<MissionDto.MissionResponse>> listAllAdmin() {
        List<Mission> missions = manageMissionUseCase.findAll();
        List<MissionDto.MissionResponse> response = missions.stream()
                .map(this::toMissionResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{missionId}/enabled")
    public ResponseEntity<MissionDto.MissionResponse> setEnabled(
            @PathVariable UUID missionId,
            @RequestBody Map<String, Boolean> body) {
        Boolean enabled = body.get("enabled");
        if (enabled == null) {
            return ResponseEntity.badRequest().build();
        }
        Mission mission = manageMissionUseCase.findById(missionId);
        ManageMissionUseCase.UpdateMissionCommand command = new ManageMissionUseCase.UpdateMissionCommand(
                missionId,
                mission.getTitle(), mission.getBriefing(), mission.getObjective(),
                mission.getHint(), mission.getDdlScript(), mission.getDmlScript(),
                mission.getTechniques(), mission.getXpReward(), mission.isOrdered(),
                mission.getTheme(), mission.getDifficulty(), mission.getExpectedResult().rows(),
                mission.getScenarioId(), mission.getOrderIndex(), enabled);
        Mission updated = manageMissionUseCase.update(command);
        return ResponseEntity.ok(toMissionResponse(updated));
    }

    @DeleteMapping("/{missionId}")
    public ResponseEntity<Void> delete(@PathVariable UUID missionId) {
        manageMissionUseCase.delete(missionId);
        return ResponseEntity.noContent().build();
    }

    private MissionDto.MissionSummary toSummary(Mission m) {
        return new MissionDto.MissionSummary(
                m.getId(), m.getTitle(), m.getScenarioTitle(), m.getTechniques(),
                m.getXpReward(), m.isOrdered(), m.getTheme(), m.getDifficulty(),
                m.getScenarioId(), m.isEnabled());
    }

    private MissionDto.MissionResponse toResponse(GetMissionsUseCase.MissionDetail detail) {
        Mission m = detail.mission();
        return new MissionDto.MissionResponse(
                m.getId(), m.getTitle(), m.getBriefing(),
                m.getObjective(), m.getHint(),
                m.getDdlScript(), m.getDmlScript(), m.getTechniques(),
                m.getXpReward(), m.isOrdered(), m.getTheme(), m.getDifficulty(),
                m.getScenarioId(), m.getScenarioTitle(), m.getOrderIndex(),
                detail.scenarioTotalMissions(), m.isEnabled(), null);
    }

    private MissionDto.MissionResponse toMissionResponse(Mission m) {
        return new MissionDto.MissionResponse(
                m.getId(), m.getTitle(), m.getBriefing(),
                m.getObjective(), m.getHint(),
                m.getDdlScript(), m.getDmlScript(), m.getTechniques(),
                m.getXpReward(), m.isOrdered(), m.getTheme(), m.getDifficulty(),
                m.getScenarioId(), m.getScenarioTitle(), m.getOrderIndex(),
                null, m.isEnabled(), m.getExpectedResult().rows());
    }
}