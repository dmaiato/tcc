package com.sqlab.application.usecase;

import com.sqlab.application.port.in.ValidateMissionUseCase;
import com.sqlab.application.port.out.ProgressRepository;
import com.sqlab.application.port.out.UserRepository;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Progress;
import com.sqlab.domain.model.ValidationResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ValidateMissionService implements ValidateMissionUseCase {

    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final MissionAccessValidator missionAccessValidator;

    public ValidateMissionService(ProgressRepository progressRepository,
                                  UserRepository userRepository,
                                  MissionAccessValidator missionAccessValidator) {
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.missionAccessValidator = missionAccessValidator;
    }

    @Override
    public ValidationResult handle(Command command) {
        Mission mission = missionAccessValidator.ensureAccessible(command.missionId(), command.userId());

        ValidationResult result = mission.validate(command.submittedTuples());

        if (result.correct() && !progressRepository.existsByUserIdAndMissionId(command.userId(), command.missionId())) {
            progressRepository.save(Progress.complete(command.userId(), command.missionId()));
            userRepository.addXp(command.userId(), mission.getXpReward());
        }

        return result;
    }
}
