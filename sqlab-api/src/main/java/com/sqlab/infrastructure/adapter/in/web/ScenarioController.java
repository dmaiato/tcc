package com.sqlab.infrastructure.adapter.in.web;

import com.sqlab.application.port.in.GetAdminScenariosUseCase;
import com.sqlab.application.port.in.GetScenariosUseCase;
import com.sqlab.application.port.in.ManageScenarioUseCase;
import com.sqlab.domain.model.Scenario;
import com.sqlab.infrastructure.adapter.in.web.dto.ScenarioDto;
import com.sqlab.infrastructure.adapter.in.web.dto.mapper.ScenarioDtoMapper;
import com.sqlab.infrastructure.adapter.in.web.util.ControllerUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ScenarioController {

    private final GetScenariosUseCase getScenariosUseCase;
    private final ManageScenarioUseCase manageScenarioUseCase;
    private final GetAdminScenariosUseCase getAdminScenariosUseCase;

    public ScenarioController(GetScenariosUseCase getScenariosUseCase,
                              ManageScenarioUseCase manageScenarioUseCase,
                              GetAdminScenariosUseCase getAdminScenariosUseCase) {
        this.getScenariosUseCase = getScenariosUseCase;
        this.manageScenarioUseCase = manageScenarioUseCase;
        this.getAdminScenariosUseCase = getAdminScenariosUseCase;
    }

    @GetMapping("/scenarios")
    public ResponseEntity<List<ScenarioDto.ScenarioSummary>> listAll(
            @AuthenticationPrincipal String userId) {
        UUID userUuid = ControllerUtils.parseUserId(userId);
        List<GetScenariosUseCase.ScenarioSummaryResult> results = getScenariosUseCase.handleEnabledWithProgress(userUuid);
        List<ScenarioDto.ScenarioSummary> response = results.stream()
                .map(ScenarioDtoMapper::toSummary)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/scenarios/{scenarioId}")
    public ResponseEntity<ScenarioDto.ScenarioDetail> findById(
            @PathVariable UUID scenarioId,
            @AuthenticationPrincipal String userId) {
        UUID userUuid = ControllerUtils.parseUserId(userId);
        var detail = getScenariosUseCase.handleDetail(scenarioId, userUuid);

        var missionItems = ScenarioDtoMapper.toMissionItems(detail.missions());

        return ResponseEntity.ok(ScenarioDtoMapper.toDetail(detail, missionItems));
    }

    @GetMapping("/admin/scenarios")
    public ResponseEntity<List<ScenarioDto.ScenarioResponse>> listAllAdmin() {
        var results = getAdminScenariosUseCase.listAll();
        List<ScenarioDto.ScenarioResponse> response = results.stream()
                .map(ScenarioDtoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/scenarios/{scenarioId}")
    public ResponseEntity<ScenarioDto.ScenarioAdminDetail> findByIdAdmin(@PathVariable UUID scenarioId) {
        var detail = getAdminScenariosUseCase.findById(scenarioId);
        var missionSummaries = ScenarioDtoMapper.toMissionSummaries(detail.missions());
        return ResponseEntity.ok(ScenarioDtoMapper.toAdminDetail(detail, missionSummaries));
    }

    @PostMapping("/admin/scenarios")
    public ResponseEntity<ScenarioDto.ScenarioResponse> create(
            @Valid @RequestBody ScenarioDto.CreateScenarioRequest request) {
        Scenario scenario = manageScenarioUseCase.create(
                new ManageScenarioUseCase.CreateScenarioCommand(
                        request.title(), request.description(), request.theme(), request.enabled(), request.requiredLevel()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ScenarioDtoMapper.toResponse(scenario, 0));
    }

    @PutMapping("/admin/scenarios/{scenarioId}")
    public ResponseEntity<ScenarioDto.ScenarioResponse> update(
            @PathVariable UUID scenarioId,
            @Valid @RequestBody ScenarioDto.UpdateScenarioRequest request) {
        Scenario scenario = manageScenarioUseCase.update(
                new ManageScenarioUseCase.UpdateScenarioCommand(
                        scenarioId, request.title(), request.description(), request.theme(), request.enabled(), request.requiredLevel()));
        int totalMissions = manageScenarioUseCase.countMissionsByScenarioId(scenarioId);
        return ResponseEntity.ok(ScenarioDtoMapper.toResponse(scenario, totalMissions));
    }

    @DeleteMapping("/admin/scenarios/{scenarioId}")
    public ResponseEntity<Void> delete(@PathVariable UUID scenarioId) {
        manageScenarioUseCase.delete(scenarioId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/admin/scenarios/{scenarioId}/missions/reorder")
    public ResponseEntity<Void> reorderMissions(
            @PathVariable UUID scenarioId,
            @Valid @RequestBody ScenarioDto.ReorderMissionsRequest request) {
        manageScenarioUseCase.reorderMissions(
                new ManageScenarioUseCase.ReorderMissionsCommand(scenarioId, request.missionIds()));
        return ResponseEntity.ok().build();
    }
}
