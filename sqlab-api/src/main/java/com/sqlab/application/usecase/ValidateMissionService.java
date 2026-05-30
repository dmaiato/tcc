package com.sqlab.application.usecase;

import com.sqlab.application.port.in.ValidateMissionUseCase;
import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.application.port.out.ProgressRepository;
import com.sqlab.application.port.out.UserRepository;
import com.sqlab.domain.exception.LevelRequiredException;
import com.sqlab.domain.exception.MissionLockedException;
import com.sqlab.domain.exception.MissionNotFoundException;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Progress;
import com.sqlab.domain.model.User;
import com.sqlab.domain.model.UserRole;
import com.sqlab.domain.model.ValidationResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ValidateMissionService implements ValidateMissionUseCase {

    private final MissionRepository missionRepository;
    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;

    public ValidateMissionService(MissionRepository missionRepository,
                                  ProgressRepository progressRepository,
                                  UserRepository userRepository) {
        this.missionRepository = missionRepository;
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ValidationResult handle(Command command) {
        Mission mission = missionRepository.findById(command.missionId())
                .orElseThrow(() -> new MissionNotFoundException(command.missionId()));

        if (!mission.isEnabled()) {
            throw new MissionNotFoundException(command.missionId());
        }

        if (mission.getScenarioId() != null
            && missionRepository.existsByScenarioIdAndEnabledFalse(mission.getScenarioId())) {
            throw new MissionNotFoundException(command.missionId());
        }

        // Level check (before scenario lock so user sees most relevant error, admins bypass)
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new MissionNotFoundException(command.missionId()));
        if (mission.getRequiredLevel() > 0
            && user.getRole() != UserRole.ADMIN
            && User.computeLevel(user.getXp()) < mission.getRequiredLevel()) {
            throw new LevelRequiredException(mission.getRequiredLevel(), User.computeLevel(user.getXp()));
        }

        if (mission.getScenarioId() != null) {
            if (mission.getOrderIndex() != null && mission.getOrderIndex() > 1) {
                boolean prevCompleted = missionRepository.isPreviousMissionCompleted(
                        command.userId(), mission.getScenarioId(), mission.getOrderIndex() - 1);
                if (!prevCompleted) {
                    throw new MissionLockedException(mission.getId(), mission.getScenarioId(), mission.getScenarioTitle());
                }
            }
        }

        ValidationResult result = mission.validate(command.submittedTuples());

        if (result.correct() && !progressRepository.existsByUserIdAndMissionId(command.userId(), command.missionId())) {
            progressRepository.save(Progress.complete(command.userId(), command.missionId()));
            // reuse user fetched earlier — no double-fetch
            user.addXp(mission.getXpReward());
            userRepository.save(user);
        }

        return result;
    }
}