package com.sqlab.infrastructure.adapter.in.web;

import com.sqlab.application.port.in.GetMissionsUseCase;
import com.sqlab.application.port.in.AdminValidateMissionUseCase;
import com.sqlab.application.port.in.ManageMissionUseCase;
import com.sqlab.application.port.in.ValidateMissionUseCase;
import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.application.port.out.ScenarioRepository;
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

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private final GetMissionsUseCase getMissionsUseCase;
    private final ValidateMissionUseCase validateMissionUseCase;
    private final ManageMissionUseCase manageMissionUseCase;
    private final AdminValidateMissionUseCase adminValidateMissionUseCase;
    private final ScenarioRepository scenarioRepository;
    private final MissionRepository missionRepository;

    public MissionController(GetMissionsUseCase getMissionsUseCase,
                             ValidateMissionUseCase validateMissionUseCase,
                             ManageMissionUseCase manageMissionUseCase,
                             AdminValidateMissionUseCase adminValidateMissionUseCase,
                             ScenarioRepository scenarioRepository,
                             MissionRepository missionRepository) {
        this.getMissionsUseCase = getMissionsUseCase;
        this.validateMissionUseCase = validateMissionUseCase;
        this.manageMissionUseCase = manageMissionUseCase;
        this.adminValidateMissionUseCase = adminValidateMissionUseCase;
        this.scenarioRepository = scenarioRepository;
        this.missionRepository = missionRepository;
    }

    @GetMapping
    public ResponseEntity<List<MissionDto.MissionSummary>> listAll(
            @RequestParam(required = false) Theme theme,
            @RequestParam(required = false) DifficultyLevel difficulty) {

        List<Mission> missions = getMissionsUseCase
                .handle(new GetMissionsUseCase.ListAllQuery(theme, difficulty));

        Map<UUID, String> scenarioTitles = new HashMap<>();
        Set<UUID> uniqueScenarioIds = missions.stream()
                .map(Mission::getScenarioId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        scenarioRepository.findAllById(uniqueScenarioIds)
                .forEach(s -> scenarioTitles.put(s.getId(), s.getTitle()));

        List<MissionDto.MissionSummary> response = missions.stream()
                .map(m -> toSummary(m, scenarioTitles.get(m.getScenarioId())))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{missionId}")
    public ResponseEntity<MissionDto.MissionResponse> findById(
            @PathVariable UUID missionId,
            @AuthenticationPrincipal String userId) {
        UUID userUuid = parseUserId(userId);
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
                new ValidateMissionUseCase.Command(parseUserId(userId), missionId, request.tuples())
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

    @DeleteMapping("/{missionId}")
    public ResponseEntity<Void> delete(@PathVariable UUID missionId) {
        manageMissionUseCase.delete(missionId);
        return ResponseEntity.noContent().build();
    }

    private MissionDto.MissionSummary toSummary(Mission m, String scenarioTitle) {
        return new MissionDto.MissionSummary(
                m.getId(), m.getTitle(), scenarioTitle, m.getTechniques(),
                m.getXpReward(), m.isOrdered(), m.getTheme(), m.getDifficulty(),
                m.getScenarioId(), m.isEnabled(), m.getRequiredLevel());
    }

    private MissionDto.MissionResponse toResponse(GetMissionsUseCase.MissionDetail detail) {
        Mission m = detail.mission();
        return new MissionDto.MissionResponse(
                m.getId(), m.getTitle(), m.getBriefing(),
                m.getObjective(), m.getHint(),
                m.getDdlScript(), m.getDmlScript(), m.getTechniques(),
                m.getXpReward(), m.isOrdered(), m.getTheme(), m.getDifficulty(),
                m.getScenarioId(), detail.scenarioTitle(), m.getOrderIndex(),
                detail.scenarioTotalMissions(), m.isEnabled(), null, m.getRequiredLevel());
    }

    private UUID parseUserId(String userId) {
        if (userId == null) return null;
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private MissionDto.MissionResponse toMissionResponse(Mission m) {
        String scenarioTitle = m.getScenarioId() != null
                ? scenarioRepository.findById(m.getScenarioId()).map(s -> s.getTitle()).orElse(null)
                : null;
        Integer scenarioTotalMissions = m.getScenarioId() != null
                ? missionRepository.countByScenarioIdAndEnabledTrue(m.getScenarioId())
                : null;
        return new MissionDto.MissionResponse(
                m.getId(), m.getTitle(), m.getBriefing(),
                m.getObjective(), m.getHint(),
                m.getDdlScript(), m.getDmlScript(), m.getTechniques(),
                m.getXpReward(), m.isOrdered(), m.getTheme(), m.getDifficulty(),
                m.getScenarioId(), scenarioTitle, m.getOrderIndex(),
                scenarioTotalMissions, m.isEnabled(), m.getExpectedResult().rows(), m.getRequiredLevel());
    }
}
