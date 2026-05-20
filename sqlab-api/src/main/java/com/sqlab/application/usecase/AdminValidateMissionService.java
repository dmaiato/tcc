package com.sqlab.application.usecase;

import com.sqlab.application.port.in.AdminValidateMissionUseCase;
import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.domain.exception.MissionNotFoundException;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.ValidationResult;
import org.springframework.stereotype.Service;

@Service
public class AdminValidateMissionService implements AdminValidateMissionUseCase {

    private final MissionRepository missionRepository;

    public AdminValidateMissionService(MissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    @Override
    public ValidationResult handle(Command command) {
        Mission mission = missionRepository.findById(command.missionId())
                .orElseThrow(() -> new MissionNotFoundException(command.missionId()));

        return mission.validate(command.submittedTuples());
    }
}
