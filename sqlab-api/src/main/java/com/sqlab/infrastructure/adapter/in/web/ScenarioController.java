package com.sqlab.infrastructure.adapter.in.web;

import com.sqlab.application.port.in.GetMissionsUseCase;
import com.sqlab.application.port.in.GetScenariosUseCase;
import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.application.port.out.ProgressRepository;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Scenario;
import com.sqlab.infrastructure.adapter.in.web.dto.ScenarioDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/scenarios")
public class ScenarioController {

    private final GetScenariosUseCase getScenariosUseCase;
    private final MissionRepository missionRepository;
    private final ProgressRepository progressRepository;

    public ScenarioController(GetScenariosUseCase getScenariosUseCase,
                              MissionRepository missionRepository,
                              ProgressRepository progressRepository) {
        this.getScenariosUseCase = getScenariosUseCase;
        this.missionRepository = missionRepository;
        this.progressRepository = progressRepository;
    }

    @GetMapping
    public ResponseEntity<List<ScenarioDto.ScenarioSummary>> listAll(
            @AuthenticationPrincipal String userId) {
        UUID userUuid = userId != null ? UUID.fromString(userId) : null;
        Set<UUID> completedIds = userUuid != null
                ? progressRepository.findCompletedMissionIdsByUserId(userUuid)
                : Set.of();
        List<Scenario> scenarios = getScenariosUseCase.handle();
        List<ScenarioDto.ScenarioSummary> response = scenarios.stream().map(s -> {
            List<Mission> missions = missionRepository.findByScenarioIdOrderByOrderIndex(s.getId());
            int completed = (int) missions.stream()
                    .filter(m -> completedIds.contains(m.getId()))
                    .count();
            return new ScenarioDto.ScenarioSummary(
                    s.getId(), s.getTitle(), s.getTheme(), missions.size(), completed);
        }).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{scenarioId}")
    public ResponseEntity<ScenarioDto.ScenarioDetail> findById(
            @PathVariable UUID scenarioId,
            @AuthenticationPrincipal String userId) {
        UUID userUuid = userId != null ? UUID.fromString(userId) : null;
        Set<UUID> completedIds = userUuid != null
                ? progressRepository.findCompletedMissionIdsByUserId(userUuid)
                : Set.of();
        Scenario scenario = getScenariosUseCase.handle(scenarioId);
        List<Mission> missions = missionRepository.findByScenarioIdOrderByOrderIndex(scenarioId);

        List<ScenarioDto.ScenarioMissionItem> missionItems = missions.stream().map(m -> {
            String status;
            boolean completed = completedIds.contains(m.getId());
            if (completed) {
                status = "COMPLETED";
            } else if (m.getOrderIndex() == null || m.getOrderIndex() == 1
                       || isPreviousMissionCompleted(completedIds, missions, m.getOrderIndex())) {
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
                Map.of("completedCount", completedCount, "totalCount", missions.size())
        ));
    }

    private boolean isPreviousMissionCompleted(Set<UUID> completedIds, List<Mission> missions, int orderIndex) {
        return missions.stream()
                .filter(m -> m.getOrderIndex() != null && m.getOrderIndex() == orderIndex - 1)
                .findFirst()
                .map(prev -> completedIds.contains(prev.getId()))
                .orElse(false);
    }
}
