package com.sqlab.application.usecase;

import com.sqlab.application.port.in.AdminValidateMissionUseCase;
import com.sqlab.application.port.out.MissionQueryPort;
import com.sqlab.domain.exception.MissionNotFoundException;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.ValidationResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminValidateMissionService implements AdminValidateMissionUseCase {

    private final MissionQueryPort missionQueryPort;

    public AdminValidateMissionService(MissionQueryPort missionQueryPort) {
        this.missionQueryPort = missionQueryPort;
    }

    @Override
    public ValidationResult handle(Command command) {
        Mission mission = missionQueryPort.findById(command.missionId())
                .orElseThrow(() -> new MissionNotFoundException(command.missionId()));

        return mission.validate(command.submittedTuples());
    }
}
