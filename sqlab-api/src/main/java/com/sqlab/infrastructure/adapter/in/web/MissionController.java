package com.sqlab.infrastructure.adapter.in.web;

import com.sqlab.application.port.in.GetAdminMissionsUseCase;
import com.sqlab.application.port.in.GetMissionsUseCase;
import com.sqlab.application.port.in.AdminValidateMissionUseCase;
import com.sqlab.application.port.in.ManageMissionUseCase;
import com.sqlab.application.port.in.ValidateMissionUseCase;
import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.domain.model.Mission;
import com.sqlab.infrastructure.adapter.in.web.dto.MissionDto;
import com.sqlab.infrastructure.adapter.in.web.dto.mapper.MissionDtoMapper;
import com.sqlab.infrastructure.adapter.in.web.util.ControllerUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    private final ManageMissionUseCase manageMissionUseCase;
    private final AdminValidateMissionUseCase adminValidateMissionUseCase;
    private final GetAdminMissionsUseCase getAdminMissionsUseCase;

    public MissionController(GetMissionsUseCase getMissionsUseCase,
                             ValidateMissionUseCase validateMissionUseCase,
                             ManageMissionUseCase manageMissionUseCase,
                             AdminValidateMissionUseCase adminValidateMissionUseCase,
                             GetAdminMissionsUseCase getAdminMissionsUseCase) {
        this.getMissionsUseCase = getMissionsUseCase;
        this.validateMissionUseCase = validateMissionUseCase;
        this.manageMissionUseCase = manageMissionUseCase;
        this.adminValidateMissionUseCase = adminValidateMissionUseCase;
        this.getAdminMissionsUseCase = getAdminMissionsUseCase;
    }

    @GetMapping
    public ResponseEntity<List<MissionDto.MissionSummary>> listAll(
            @RequestParam(required = false) String theme,
            @RequestParam(required = false) DifficultyLevel difficulty) {

        List<Mission> missions = getMissionsUseCase
                .handle(new GetMissionsUseCase.ListAllQuery(theme, difficulty));

        List<MissionDto.MissionSummary> response = missions.stream()
                .map(m -> MissionDtoMapper.toSummary(m, null))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{missionId}")
    public ResponseEntity<MissionDto.MissionResponse> findById(
            @PathVariable UUID missionId,
            @AuthenticationPrincipal String userId) {
        UUID userUuid = ControllerUtils.parseUserId(userId);
        GetMissionsUseCase.MissionDetail detail = getMissionsUseCase.handleDetail(
                new GetMissionsUseCase.FindByIdQuery(missionId, userUuid));
        return ResponseEntity.ok(MissionDtoMapper.toResponse(detail));
    }

    @PostMapping("/{missionId}/validate")
    public ResponseEntity<MissionDto.ValidationResponse> validate(
            @PathVariable UUID missionId,
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody MissionDto.ValidationRequest request) {

        var result = validateMissionUseCase.handle(
                new ValidateMissionUseCase.Command(ControllerUtils.parseUserId(userId), missionId, request.tuples())
        );
        return ResponseEntity.ok(MissionDtoMapper.toValidationResponse(result));
    }

    @PostMapping("/{missionId}/validate/admin")
    public ResponseEntity<MissionDto.ValidationResponse> adminValidate(
            @PathVariable UUID missionId,
            @Valid @RequestBody MissionDto.ValidationRequest request) {

        var result = adminValidateMissionUseCase.handle(
                new AdminValidateMissionUseCase.Command(missionId, request.tuples())
        );
        return ResponseEntity.ok(MissionDtoMapper.toValidationResponse(result));
    }

    @PostMapping
    public ResponseEntity<MissionDto.MissionResponse> create(
            @Valid @RequestBody MissionDto.UpsertMissionRequest request) {
        ManageMissionUseCase.CreateMissionCommand command = new ManageMissionUseCase.CreateMissionCommand(
                request.title(), request.briefing(), request.objective(),
                request.hint(), request.ddlScript(), request.dmlScript(),
                request.techniques(), request.xpReward(), request.ordered(),
                request.theme(), request.difficulty(), request.expectedResult(),
                request.scenarioId(), request.orderIndex(),
                request.enabled());
        Mission mission = manageMissionUseCase.create(command);
        var result = new GetAdminMissionsUseCase.AdminMissionResult(mission, null, null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MissionDtoMapper.toMissionResponse(result));
    }

    @PutMapping("/{missionId}")
    public ResponseEntity<MissionDto.MissionResponse> update(
            @PathVariable UUID missionId,
            @Valid @RequestBody MissionDto.UpsertMissionRequest request) {
        ManageMissionUseCase.UpdateMissionCommand command = new ManageMissionUseCase.UpdateMissionCommand(
                missionId,
                request.title(), request.briefing(), request.objective(),
                request.hint(), request.ddlScript(), request.dmlScript(),
                request.techniques(), request.xpReward(), request.ordered(),
                request.theme(), request.difficulty(), request.expectedResult(),
                request.scenarioId(), request.orderIndex(),
                request.enabled());
        Mission mission = manageMissionUseCase.update(command);
        var result = new GetAdminMissionsUseCase.AdminMissionResult(mission, null, null);
        return ResponseEntity.ok(MissionDtoMapper.toMissionResponse(result));
    }

    @GetMapping("/{missionId}/admin")
    public ResponseEntity<MissionDto.MissionResponse> findByIdAdmin(@PathVariable UUID missionId) {
        var result = getAdminMissionsUseCase.findById(missionId);
        return ResponseEntity.ok(MissionDtoMapper.toMissionResponse(result));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<MissionDto.MissionResponse>> listAllAdmin() {
        var results = getAdminMissionsUseCase.listAll();
        List<MissionDto.MissionResponse> response = results.stream()
                .map(MissionDtoMapper::toMissionResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{missionId}")
    public ResponseEntity<Void> delete(@PathVariable UUID missionId) {
        manageMissionUseCase.delete(missionId);
        return ResponseEntity.noContent().build();
    }
}
