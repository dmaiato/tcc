package com.sqlab.infrastructure.adapter.in.web;

import com.sqlab.application.port.in.GetMissionsUseCase;
import com.sqlab.application.port.in.GetScenariosUseCase;
import com.sqlab.application.port.in.ManageScenarioUseCase;
import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.application.port.out.ProgressRepository;
import com.sqlab.application.port.out.UserRepository;
import com.sqlab.domain.exception.ScenarioNotFoundException;
import com.sqlab.domain.model.User;
import com.sqlab.domain.model.UserRole;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Scenario;
import com.sqlab.infrastructure.adapter.in.web.dto.ScenarioDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ScenarioController {

    private final GetScenariosUseCase getScenariosUseCase;
    private final ManageScenarioUseCase manageScenarioUseCase;
    private final MissionRepository missionRepository;
    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;

    public ScenarioController(GetScenariosUseCase getScenariosUseCase,
                              ManageScenarioUseCase manageScenarioUseCase,
                              MissionRepository missionRepository,
                              ProgressRepository progressRepository,
                              UserRepository userRepository) {
        this.getScenariosUseCase = getScenariosUseCase;
        this.manageScenarioUseCase = manageScenarioUseCase;
        this.missionRepository = missionRepository;
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/scenarios")
    public ResponseEntity<List<ScenarioDto.ScenarioSummary>> listAll(
            @AuthenticationPrincipal String userId) {
        UUID userUuid = parseUserId(userId);
        Set<UUID> completedIds = userUuid != null
                ? progressRepository.findCompletedMissionIdsByUserId(userUuid)
                : Set.of();
        List<Scenario> scenarios = getScenariosUseCase.handleEnabled();
        Set<UUID> allScenarioIds = scenarios.stream().map(Scenario::getId).collect(Collectors.toSet());
        Map<UUID, List<Mission>> missionsByScenario = missionRepository
                .findByScenarioIdInOrderByOrderIndex(allScenarioIds)
                .stream()
                .collect(Collectors.groupingBy(Mission::getScenarioId, LinkedHashMap::new, Collectors.toList()));
        List<ScenarioDto.ScenarioSummary> response = scenarios.stream()
                .filter(s -> {
                    List<Mission> missions = missionsByScenario.getOrDefault(s.getId(), List.of());
                    return !missions.isEmpty() && missions.stream().allMatch(Mission::isEnabled);
                })
                .map(s -> {
                    List<Mission> missions = missionsByScenario.get(s.getId());
                    int completed = (int) missions.stream()
                            .filter(m -> completedIds.contains(m.getId()))
                            .count();
                    return new ScenarioDto.ScenarioSummary(
                            s.getId(), s.getTitle(), s.getTheme(), missions.size(), completed, s.getRequiredLevel());
                })
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/scenarios/{scenarioId}")
    public ResponseEntity<ScenarioDto.ScenarioDetail> findById(
            @PathVariable UUID scenarioId,
            @AuthenticationPrincipal String userId) {
        UUID userUuid = parseUserId(userId);
        Set<UUID> completedIds = userUuid != null
                ? progressRepository.findCompletedMissionIdsByUserId(userUuid)
                : Set.of();
        Scenario scenario = getScenariosUseCase.handle(scenarioId);
        List<Mission> allMissions = missionRepository.findByScenarioIdOrderByOrderIndex(scenarioId);
        if (allMissions.isEmpty() || allMissions.stream().anyMatch(m -> !m.isEnabled())) {
            throw new ScenarioNotFoundException(scenarioId);
        }

        // Compute user level for level check (admins bypass)
        boolean levelRestricted = scenario.getRequiredLevel() > 0;
        boolean belowRequiredLevel = levelRestricted && userUuid != null
                ? userRepository.findById(userUuid)
                        .map(u -> u.getRole() != UserRole.ADMIN && User.computeLevel(u.getXp()) < scenario.getRequiredLevel())
                        .orElse(false)
                : false;

        List<ScenarioDto.ScenarioMissionItem> missionItems = allMissions.stream().map(m -> {
            String status;
            boolean completed = completedIds.contains(m.getId());
            if (completed) {
                status = "COMPLETED";
            } else if (belowRequiredLevel) {
                status = "LOCKED";
            } else if (m.getOrderIndex() == null || m.getOrderIndex() == 1
                       || isPreviousMissionCompleted(completedIds, allMissions, m.getOrderIndex())) {
                status = "AVAILABLE";
            } else {
                status = "LOCKED";
            }
            return new ScenarioDto.ScenarioMissionItem(
                    m.getId(), m.getTitle(), m.getTechniques(),
                    m.getXpReward(), m.getDifficulty(), status, m.getRequiredLevel());
        }).toList();

        int completedCount = (int) missionItems.stream()
                .filter(i -> "COMPLETED".equals(i.status()))
                .count();

        return ResponseEntity.ok(new ScenarioDto.ScenarioDetail(
                scenario.getId(), scenario.getTitle(), scenario.getDescription(),
                scenario.getTheme(), missionItems,
                Map.of("completedCount", completedCount, "totalCount", allMissions.size()),
                scenario.getRequiredLevel()
        ));
    }

    @GetMapping("/admin/scenarios")
    public ResponseEntity<List<ScenarioDto.ScenarioResponse>> listAllAdmin() {
        List<Scenario> scenarios = getScenariosUseCase.handle();
        List<ScenarioDto.ScenarioResponse> response = scenarios.stream().map(s ->
                new ScenarioDto.ScenarioResponse(
                        s.getId(), s.getTitle(), s.getDescription(), s.getTheme(),
                        missionRepository.countByScenarioId(s.getId()), s.isEnabled(), s.getRequiredLevel())
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
                scenario.getTheme(), scenario.isEnabled(), missionSummaries.size(), missionSummaries, scenario.getRequiredLevel()));
    }

    @PostMapping("/admin/scenarios")
    public ResponseEntity<ScenarioDto.ScenarioResponse> create(
            @Valid @RequestBody ScenarioDto.CreateScenarioRequest request) {
        Scenario scenario = manageScenarioUseCase.create(
                new ManageScenarioUseCase.CreateScenarioCommand(
                        request.title(), request.description(), request.theme(), request.enabled(), request.requiredLevel()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ScenarioDto.ScenarioResponse(
                        scenario.getId(), scenario.getTitle(), scenario.getDescription(),
                        scenario.getTheme(), 0, scenario.isEnabled(), scenario.getRequiredLevel()));
    }

    @PutMapping("/admin/scenarios/{scenarioId}")
    public ResponseEntity<ScenarioDto.ScenarioResponse> update(
            @PathVariable UUID scenarioId,
            @Valid @RequestBody ScenarioDto.UpdateScenarioRequest request) {
        Scenario scenario = manageScenarioUseCase.update(
                new ManageScenarioUseCase.UpdateScenarioCommand(
                        scenarioId, request.title(), request.description(), request.theme(), request.enabled(), request.requiredLevel()));
        int count = missionRepository.countByScenarioId(scenarioId);
        return ResponseEntity.ok(new ScenarioDto.ScenarioResponse(
                scenario.getId(), scenario.getTitle(), scenario.getDescription(),
                scenario.getTheme(), count, scenario.isEnabled(), scenario.getRequiredLevel()));
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

    private UUID parseUserId(String userId) {
        if (userId == null) return null;
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private boolean isPreviousMissionCompleted(Set<UUID> completedIds, List<Mission> missions, int orderIndex) {
        return missions.stream()
                .filter(m -> m.getOrderIndex() != null && m.getOrderIndex() == orderIndex - 1)
                .findFirst()
                .map(prev -> completedIds.contains(prev.getId()))
                .orElse(false);
    }
}
