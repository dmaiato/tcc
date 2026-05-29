package com.sqlab.infrastructure.adapter.in.web;

import com.sqlab.application.port.in.GetMissionsUseCase;
import com.sqlab.application.port.in.GetScenariosUseCase;
import com.sqlab.application.port.in.ManageScenarioUseCase;
import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.application.port.out.ProgressRepository;
import com.sqlab.domain.exception.ScenarioNotFoundException;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Scenario;
import com.sqlab.infrastructure.adapter.in.web.dto.ScenarioDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ScenarioController {

    private final GetScenariosUseCase getScenariosUseCase;
    private final ManageScenarioUseCase manageScenarioUseCase;
    private final MissionRepository missionRepository;
    private final ProgressRepository progressRepository;

    public ScenarioController(GetScenariosUseCase getScenariosUseCase,
                              ManageScenarioUseCase manageScenarioUseCase,
                              MissionRepository missionRepository,
                              ProgressRepository progressRepository) {
        this.getScenariosUseCase = getScenariosUseCase;
        this.manageScenarioUseCase = manageScenarioUseCase;
        this.missionRepository = missionRepository;
        this.progressRepository = progressRepository;
    }

    @GetMapping("/scenarios")
    public ResponseEntity<List<ScenarioDto.ScenarioSummary>> listAll(
            @AuthenticationPrincipal String userId) {
        UUID userUuid = userId != null ? UUID.fromString(userId) : null;
        Set<UUID> completedIds = userUuid != null
                ? progressRepository.findCompletedMissionIdsByUserId(userUuid)
                : Set.of();
        List<Scenario> scenarios = getScenariosUseCase.handle();
        List<ScenarioDto.ScenarioSummary> response = scenarios.stream()
                .map(s -> {
                    List<Mission> missions = missionRepository.findByScenarioIdOrderByOrderIndex(s.getId());
                    return new AbstractMap.SimpleEntry<>(s, missions);
                })
                .filter(entry -> {
                    List<Mission> missions = entry.getValue();
                    return !missions.isEmpty() && missions.stream().allMatch(Mission::isEnabled);
                })
                .map(entry -> {
                    Scenario s = entry.getKey();
                    List<Mission> missions = entry.getValue();
                    int completed = (int) missions.stream()
                            .filter(m -> completedIds.contains(m.getId()))
                            .count();
                    return new ScenarioDto.ScenarioSummary(
                            s.getId(), s.getTitle(), s.getTheme(), missions.size(), completed);
                })
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/scenarios/{scenarioId}")
    public ResponseEntity<ScenarioDto.ScenarioDetail> findById(
            @PathVariable UUID scenarioId,
            @AuthenticationPrincipal String userId) {
        UUID userUuid = userId != null ? UUID.fromString(userId) : null;
        Set<UUID> completedIds = userUuid != null
                ? progressRepository.findCompletedMissionIdsByUserId(userUuid)
                : Set.of();
        Scenario scenario = getScenariosUseCase.handle(scenarioId);
        List<Mission> allMissions = missionRepository.findByScenarioIdOrderByOrderIndex(scenarioId);
        if (allMissions.isEmpty() || allMissions.stream().anyMatch(m -> !m.isEnabled())) {
            throw new ScenarioNotFoundException(scenarioId);
        }

        List<ScenarioDto.ScenarioMissionItem> missionItems = allMissions.stream().map(m -> {
            String status;
            boolean completed = completedIds.contains(m.getId());
            if (completed) {
                status = "COMPLETED";
            } else if (m.getOrderIndex() == null || m.getOrderIndex() == 1
                       || isPreviousMissionCompleted(completedIds, allMissions, m.getOrderIndex())) {
                status = "AVAILABLE";
            } else {
                status = "LOCKED";
            }
            return new ScenarioDto.ScenarioMissionItem(
                    m.getId(), m.getTitle(), m.getTechniques(),
                    m.getXpReward(), m.getDifficulty(), status);
        }).toList();

        int completedCount = (int) missionItems.stream()
                .filter(i -> "COMPLETED".equals(i.status()))
                .count();

        return ResponseEntity.ok(new ScenarioDto.ScenarioDetail(
                scenario.getId(), scenario.getTitle(), scenario.getDescription(),
                scenario.getTheme(), missionItems,
                Map.of("completedCount", completedCount, "totalCount", allMissions.size())
        ));
    }

    @GetMapping("/admin/scenarios")
    public ResponseEntity<List<ScenarioDto.ScenarioResponse>> listAllAdmin() {
        List<Scenario> scenarios = getScenariosUseCase.handle();
        List<ScenarioDto.ScenarioResponse> response = scenarios.stream().map(s ->
                new ScenarioDto.ScenarioResponse(
                        s.getId(), s.getTitle(), s.getDescription(), s.getTheme(),
                        missionRepository.countByScenarioId(s.getId()), s.isEnabled())
        ).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/scenarios/{scenarioId}")
    public ResponseEntity<ScenarioDto.ScenarioAdminDetail> findByIdAdmin(@PathVariable UUID scenarioId) {
        Scenario scenario = getScenariosUseCase.handle(scenarioId);
        List<Mission> missions = missionRepository.findByScenarioIdOrderByOrderIndex(scenarioId);
        List<ScenarioDto.ScenarioMissionSummary> missionSummaries = missions.stream()
                .map(m -> new ScenarioDto.ScenarioMissionSummary(
                        m.getId(), m.getTitle(), m.getDifficulty(), m.getXpReward(), m.isEnabled()))
                .toList();
        return ResponseEntity.ok(new ScenarioDto.ScenarioAdminDetail(
                scenario.getId(), scenario.getTitle(), scenario.getDescription(),
                scenario.getTheme(), scenario.isEnabled(), missionSummaries.size(), missionSummaries));
    }

    @PostMapping("/admin/scenarios")
    public ResponseEntity<ScenarioDto.ScenarioResponse> create(
            @Valid @RequestBody ScenarioDto.CreateScenarioRequest request) {
        Scenario scenario = manageScenarioUseCase.create(
                new ManageScenarioUseCase.CreateScenarioCommand(
                        request.title(), request.description(), request.theme(), request.enabled()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ScenarioDto.ScenarioResponse(
                        scenario.getId(), scenario.getTitle(), scenario.getDescription(),
                        scenario.getTheme(), 0, scenario.isEnabled()));
    }

    @PutMapping("/admin/scenarios/{scenarioId}")
    public ResponseEntity<ScenarioDto.ScenarioResponse> update(
            @PathVariable UUID scenarioId,
            @Valid @RequestBody ScenarioDto.UpdateScenarioRequest request) {
        Scenario scenario = manageScenarioUseCase.update(
                new ManageScenarioUseCase.UpdateScenarioCommand(
                        scenarioId, request.title(), request.description(), request.theme(), request.enabled()));
        int count = missionRepository.countByScenarioId(scenarioId);
        return ResponseEntity.ok(new ScenarioDto.ScenarioResponse(
                scenario.getId(), scenario.getTitle(), scenario.getDescription(),
                scenario.getTheme(), count, scenario.isEnabled()));
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

    private boolean isPreviousMissionCompleted(Set<UUID> completedIds, List<Mission> missions, int orderIndex) {
        return missions.stream()
                .filter(m -> m.getOrderIndex() != null && m.getOrderIndex() == orderIndex - 1)
                .findFirst()
                .map(prev -> completedIds.contains(prev.getId()))
                .orElse(false);
    }
}
