package com.sqlab.application.usecase;

import com.sqlab.application.port.out.MissionQueryPort;
import com.sqlab.application.port.out.MissionValidationPort;
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

    private final MissionQueryPort missionQueryPort;
    private final MissionValidationPort missionValidationPort;
    private final UserRepository userRepository;
    private final ScenarioRepository scenarioRepository;

    public MissionAccessValidator(MissionQueryPort missionQueryPort,
                                  MissionValidationPort missionValidationPort,
                                  UserRepository userRepository,
                                  ScenarioRepository scenarioRepository) {
        this.missionQueryPort = missionQueryPort;
        this.missionValidationPort = missionValidationPort;
        this.userRepository = userRepository;
        this.scenarioRepository = scenarioRepository;
    }

    public Mission ensureAccessible(UUID missionId, UUID userId) {
        Mission mission = missionQueryPort.findById(missionId)
                .orElseThrow(() -> new MissionNotFoundException(missionId));

        if (!mission.isEnabled()) {
            throw new MissionNotFoundException(missionId);
        }

        if (mission.getScenarioId() != null
            && missionValidationPort.existsByScenarioIdAndEnabledFalse(mission.getScenarioId())) {
            throw new MissionNotFoundException(missionId);
        }

        checkLevel(mission, userId);
        checkOrder(mission, userId);

        return mission;
    }

    public Mission checkLevel(UUID missionId, UUID userId) {
        Mission mission = missionQueryPort.findById(missionId)
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
            boolean prevCompleted = missionValidationPort.isPreviousMissionCompleted(
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
