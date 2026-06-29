package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetScenariosUseCase;
import com.sqlab.application.port.out.MissionQueryPort;
import com.sqlab.application.port.out.MissionValidationPort;
import com.sqlab.application.port.out.ProgressRepository;
import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.application.port.out.UserRepository;
import com.sqlab.domain.exception.ScenarioNotFoundException;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Page;
import com.sqlab.domain.model.Scenario;
import com.sqlab.domain.model.User;
import com.sqlab.domain.model.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetScenariosService implements GetScenariosUseCase {

    private final ScenarioRepository scenarioRepository;
    private final MissionQueryPort missionQueryPort;
    private final MissionValidationPort missionValidationPort;
    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;

    public GetScenariosService(ScenarioRepository scenarioRepository,
                                MissionQueryPort missionQueryPort,
                                MissionValidationPort missionValidationPort,
                                ProgressRepository progressRepository,
                                UserRepository userRepository) {
        this.scenarioRepository = scenarioRepository;
        this.missionQueryPort = missionQueryPort;
        this.missionValidationPort = missionValidationPort;
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Scenario> handle() {
        return scenarioRepository.findAll();
    }

    @Override
    public List<Scenario> handleEnabled() {
        return scenarioRepository.findByEnabled();
    }

    @Override
    public List<ScenarioSummaryResult> handleEnabledWithProgress(UUID userId) {
        Set<UUID> completedIds = userId != null
                ? progressRepository.findCompletedMissionIdsByUserId(userId)
                : Set.of();
        List<Scenario> scenarios = scenarioRepository.findByEnabled();
        Set<UUID> allScenarioIds = scenarios.stream().map(Scenario::getId).collect(Collectors.toSet());
        Map<UUID, List<Mission>> missionsByScenario = missionQueryPort
                .findByScenarioIdInOrderByOrderIndex(allScenarioIds)
                .stream()
                .collect(Collectors.groupingBy(Mission::getScenarioId, LinkedHashMap::new, Collectors.toList()));
        return scenarios.stream()
                .filter(s -> {
                    List<Mission> missions = missionsByScenario.getOrDefault(s.getId(), List.of());
                    return !missions.isEmpty() && missions.stream().allMatch(Mission::isEnabled);
                })
                .map(s -> {
                    List<Mission> missions = missionsByScenario.get(s.getId());
                    int completed = (int) missions.stream()
                            .filter(m -> completedIds.contains(m.getId()))
                            .count();
                    return new ScenarioSummaryResult(
                            s.getId(), s.getTitle(), missions.size(), completed, s.getRequiredLevel(), s.getTheme());
                })
                .toList();
    }

    @Override
    public Page<ScenarioSummaryResult> handleEnabledWithProgress(UUID userId, String name, String themeName, int page, int size) {
        Set<UUID> completedIds = userId != null
                ? progressRepository.findCompletedMissionIdsByUserId(userId)
                : Set.of();
        com.sqlab.domain.model.Page<Scenario> scenarioPage = scenarioRepository.findByFilters(name, themeName, page, size);
        Set<UUID> pageScenarioIds = scenarioPage.content().stream()
                .map(Scenario::getId)
                .collect(Collectors.toSet());
        Map<UUID, List<Mission>> missionsByScenario = pageScenarioIds.isEmpty()
                ? Map.of()
                : missionQueryPort.findByScenarioIdInOrderByOrderIndex(pageScenarioIds)
                        .stream()
                        .collect(Collectors.groupingBy(Mission::getScenarioId, LinkedHashMap::new, Collectors.toList()));
        List<ScenarioSummaryResult> results = scenarioPage.content().stream()
                .map(s -> {
                    List<Mission> missions = missionsByScenario.getOrDefault(s.getId(), List.of());
                    int completed = (int) missions.stream()
                            .filter(m -> completedIds.contains(m.getId()))
                            .count();
                    return new ScenarioSummaryResult(
                            s.getId(), s.getTitle(), missions.size(), completed, s.getRequiredLevel(), s.getTheme());
                })
                .toList();
        return new Page<>(results, scenarioPage.totalElements(), scenarioPage.totalPages(), scenarioPage.number(), scenarioPage.size());
    }

    @Override
    public Scenario handle(UUID id) {
        return scenarioRepository.findById(id)
                .orElseThrow(() -> new ScenarioNotFoundException(id));
    }

    @Override
    public ScenarioDetailResult handleDetail(UUID scenarioId, UUID userId) {
        Set<UUID> completedIds = userId != null
                ? progressRepository.findCompletedMissionIdsByUserId(userId)
                : Set.of();

        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new ScenarioNotFoundException(scenarioId));

        List<Mission> allMissions = missionQueryPort.findByScenarioIdOrderByOrderIndex(scenarioId);
        if (allMissions.isEmpty() || allMissions.stream().anyMatch(m -> !m.isEnabled())) {
            throw new ScenarioNotFoundException(scenarioId);
        }

        boolean levelRestricted = scenario.getRequiredLevel() > 0;
        boolean belowRequiredLevel = levelRestricted && userId != null
                ? userRepository.findById(userId)
                        .map(u -> u.getRole() != UserRole.ADMIN && User.computeLevel(u.getXp()) < scenario.getRequiredLevel())
                        .orElse(false)
                : false;

        List<MissionStatus> missionStatuses = allMissions.stream().map(m -> {
            String status;
            boolean completed = completedIds.contains(m.getId());
            if (completed) {
                status = "COMPLETED";
            } else if (belowRequiredLevel) {
                status = "LOCKED";
            } else if (m.getOrderIndex() == null || m.getOrderIndex() == 1
                       || missionValidationPort.isPreviousMissionCompleted(userId, scenarioId, m.getOrderIndex() - 1)) {
                status = "AVAILABLE";
            } else {
                status = "LOCKED";
            }
            return new MissionStatus(m, status);
        }).toList();

        int completedCount = (int) missionStatuses.stream()
                .filter(ms -> "COMPLETED".equals(ms.status()))
                .count();

        return new ScenarioDetailResult(scenario, missionStatuses, completedCount, allMissions.size());
    }
}
