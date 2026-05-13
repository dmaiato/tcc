package com.sqlab.application.usecase;

import com.sqlab.application.port.in.ValidateMissionUseCase;
import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.application.port.out.ProgressRepository;
import com.sqlab.application.port.out.UserRepository;
import com.sqlab.domain.exception.MissionLockedException;
import com.sqlab.domain.exception.MissionNotFoundException;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Progress;
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
    public boolean handle(Command command) {
        Mission mission = missionRepository.findById(command.missionId())
                .orElseThrow(() -> new MissionNotFoundException(command.missionId()));

        if (mission.getScenarioId() != null) {
            if (mission.getOrderIndex() != null && mission.getOrderIndex() > 1) {
                boolean prevCompleted = missionRepository.isPreviousMissionCompleted(
                        command.userId(), mission.getScenarioId(), mission.getOrderIndex() - 1);
                if (!prevCompleted) {
                    throw new MissionLockedException(mission.getId(), mission.getScenarioId(), mission.getScenarioTitle());
                }
            }
        }

        boolean correct = mission.validate(command.submittedTuples());

        if (correct && !progressRepository.existsByUserIdAndMissionId(command.userId(), command.missionId())) {
            progressRepository.save(Progress.complete(command.userId(), command.missionId()));

            userRepository.findById(command.userId()).ifPresent(user -> {
                user.addXp(mission.getXpReward());
                userRepository.save(user);
            });
        }

        return correct;
    }
}