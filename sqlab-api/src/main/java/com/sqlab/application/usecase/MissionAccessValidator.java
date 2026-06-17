package com.sqlab.application.usecase;

import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.application.port.out.UserRepository;
import com.sqlab.domain.exception.LevelRequiredException;
import com.sqlab.domain.exception.MissionLockedException;
import com.sqlab.domain.exception.MissionNotFoundException;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.User;
import com.sqlab.domain.model.UserRole;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MissionAccessValidator {

    private final MissionRepository missionRepository;
    private final UserRepository userRepository;
    private final ScenarioRepository scenarioRepository;

    public MissionAccessValidator(MissionRepository missionRepository,
                                  UserRepository userRepository,
                                  ScenarioRepository scenarioRepository) {
        this.missionRepository = missionRepository;
        this.userRepository = userRepository;
        this.scenarioRepository = scenarioRepository;
    }

    public Mission ensureAccessible(UUID missionId, UUID userId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionNotFoundException(missionId));

        if (!mission.isEnabled()) {
            throw new MissionNotFoundException(missionId);
        }

        if (mission.getScenarioId() != null
            && missionRepository.existsByScenarioIdAndEnabledFalse(mission.getScenarioId())) {
            throw new MissionNotFoundException(missionId);
        }

        checkLevel(mission, userId);
        checkOrder(mission, userId);

        return mission;
    }

    public Mission checkLevel(UUID missionId, UUID userId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionNotFoundException(missionId));
        checkLevel(mission, userId);
        return mission;
    }

    private void checkLevel(Mission mission, UUID userId) {
        if (mission.getRequiredLevel() > 0 && userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new MissionNotFoundException(mission.getId()));
            if (user.getRole() != UserRole.ADMIN
                && User.computeLevel(user.getXp()) < mission.getRequiredLevel()) {
                throw new LevelRequiredException(mission.getRequiredLevel(), User.computeLevel(user.getXp()));
            }
        }
    }

    private void checkOrder(Mission mission, UUID userId) {
        if (userId != null && mission.getScenarioId() != null
            && mission.getOrderIndex() != null && mission.getOrderIndex() > 1) {
            boolean prevCompleted = missionRepository.isPreviousMissionCompleted(
                    userId, mission.getScenarioId(), mission.getOrderIndex() - 1);
            if (!prevCompleted) {
                String scenarioTitle = scenarioRepository.findById(mission.getScenarioId())
                        .map(s -> s.getTitle())
                        .orElse("");
                throw new MissionLockedException(mission.getId(), mission.getScenarioId(), scenarioTitle);
            }
        }
    }
}
