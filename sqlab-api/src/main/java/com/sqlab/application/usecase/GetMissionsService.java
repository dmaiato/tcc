package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetMissionsUseCase;
import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.application.port.out.UserRepository;
import com.sqlab.domain.exception.LevelRequiredException;
import com.sqlab.domain.exception.MissionLockedException;
import com.sqlab.domain.exception.MissionNotFoundException;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.User;
import com.sqlab.domain.model.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetMissionsService implements GetMissionsUseCase {

    private final MissionRepository missionRepository;
    private final UserRepository userRepository;
    private final ScenarioRepository scenarioRepository;

    public GetMissionsService(MissionRepository missionRepository,
                              UserRepository userRepository,
                              ScenarioRepository scenarioRepository) {
        this.missionRepository = missionRepository;
        this.userRepository = userRepository;
        this.scenarioRepository = scenarioRepository;
    }

    @Override
    public List<Mission> handle(ListAllQuery query) {
        List<Mission> missions;
        if (query.theme() != null && query.difficulty() != null) {
            missions = missionRepository.findByThemeAndDifficulty(query.theme(), query.difficulty());
        } else if (query.theme() != null) {
            missions = missionRepository.findByTheme(query.theme());
        } else if (query.difficulty() != null) {
            missions = missionRepository.findByDifficulty(query.difficulty());
        } else {
            missions = missionRepository.findByEnabledTrue();
        }

        List<Mission> enabledMissions = missions.stream()
                .filter(Mission::isEnabled)
                .toList();

        Set<UUID> scenarioIds = enabledMissions.stream()
                .map(Mission::getScenarioId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<UUID> disabledScenarios = missionRepository.findScenarioIdsWithDisabledMissions(scenarioIds);

        return enabledMissions.stream()
                .filter(m -> m.getScenarioId() == null || !disabledScenarios.contains(m.getScenarioId()))
                .toList();
    }

    @Override
    public Mission handle(FindByIdQuery query) {
        Mission mission = missionRepository.findById(query.missionId())
                .orElseThrow(() -> new MissionNotFoundException(query.missionId()));

        if (!mission.isEnabled()) {
            throw new MissionNotFoundException(query.missionId());
        }

        if (mission.getScenarioId() != null
            && missionRepository.existsByScenarioIdAndEnabledFalse(mission.getScenarioId())) {
            throw new MissionNotFoundException(query.missionId());
        }

        if (query.userId() != null && mission.getRequiredLevel() > 0) {
            User user = userRepository.findById(query.userId())
                    .orElseThrow(() -> new MissionNotFoundException(query.missionId()));
            if (user.getRole() != UserRole.ADMIN) {
                int userLevel = User.computeLevel(user.getXp());
                if (userLevel < mission.getRequiredLevel()) {
                    throw new LevelRequiredException(mission.getRequiredLevel(), userLevel);
                }
            }
        }

        if (query.userId() != null && mission.getScenarioId() != null) {
            if (mission.getOrderIndex() != null && mission.getOrderIndex() > 1) {
                boolean prevCompleted = missionRepository.isPreviousMissionCompleted(
                        query.userId(), mission.getScenarioId(), mission.getOrderIndex() - 1);
                if (!prevCompleted) {
                    String scenarioTitle = scenarioRepository.findById(mission.getScenarioId())
                            .map(s -> s.getTitle())
                            .orElse("");
                    throw new MissionLockedException(
                            mission.getId(), mission.getScenarioId(), scenarioTitle);
                }
            }
        }

        return mission;
    }

    @Override
    public MissionDetail handleDetail(FindByIdQuery query) {
        Mission mission = handle(query);
        Integer scenarioTotalMissions = null;
        String scenarioTitle = null;
        if (mission.getScenarioId() != null) {
            scenarioTotalMissions = missionRepository.countByScenarioIdAndEnabledTrue(mission.getScenarioId());
            scenarioTitle = scenarioRepository.findById(mission.getScenarioId())
                    .map(s -> s.getTitle())
                    .orElse(null);
        }
        return new MissionDetail(mission, scenarioTotalMissions, scenarioTitle);
    }
}
