package com.sqlab.infrastructure.adapter.in.web;

import com.sqlab.application.port.in.GetAdminScenariosUseCase;
import com.sqlab.application.port.in.GetScenariosUseCase;
import com.sqlab.application.port.in.ManageScenarioUseCase;
import com.sqlab.domain.model.Page;
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

    @GetMapping(value = "/scenarios", params = "page")
    public ResponseEntity<ScenarioDto.ScenarioPage> listAllPaginated(
            @RequestParam int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String theme,
            @AuthenticationPrincipal String userId) {
        UUID userUuid = ControllerUtils.parseUserId(userId);
        Page<GetScenariosUseCase.ScenarioSummaryResult> results = getScenariosUseCase
                .handleEnabledWithProgress(userUuid, name, theme, page, size);
        return ResponseEntity.ok(ScenarioDtoMapper.toPage(results));
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

    @GetMapping(value = "/admin/scenarios", params = "!page")
    public ResponseEntity<List<ScenarioDto.ScenarioResponse>> listAllAdminFlat() {
        var results = getAdminScenariosUseCase.listAll();
        return ResponseEntity.ok(results.stream().map(ScenarioDtoMapper::toResponse).toList());
    }

    @GetMapping(value = "/admin/scenarios", params = "page")
    public ResponseEntity<ScenarioDto.AdminScenarioPage> listAllAdmin(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String theme,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        var results = getAdminScenariosUseCase.listAll(name, theme, enabled, page, size);
        return ResponseEntity.ok(ScenarioDtoMapper.toAdminPage(results));
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
