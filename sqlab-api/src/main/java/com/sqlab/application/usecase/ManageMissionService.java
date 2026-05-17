package com.sqlab.application.usecase;

import com.sqlab.application.port.in.ManageMissionUseCase;
import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.domain.exception.MissionNotFoundException;
import com.sqlab.domain.model.ExpectedTuple;
import com.sqlab.domain.model.Mission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ManageMissionService implements ManageMissionUseCase {

    private final MissionRepository missionRepository;

    public ManageMissionService(MissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    private void validateScenarioConstraint(UUID scenarioId, Integer orderIndex) {
        if (scenarioId != null && orderIndex == null) {
            throw new IllegalArgumentException("orderIndex is required when scenarioId is provided");
        }
    }

    @Override
    public Mission create(CreateMissionCommand command) {
        UUID scenarioId = command.scenarioId();
        Integer orderIndex = command.orderIndex();
        if (scenarioId != null && orderIndex == null) {
            orderIndex = missionRepository.countByScenarioId(scenarioId) + 1;
        }
        Mission mission = Mission.builder()
                .id(UUID.randomUUID())
                .title(command.title())
                .briefing(command.briefing())
                .objective(command.objective())
                .hint(command.hint())
                .ddlScript(command.ddlScript())
                .dmlScript(command.dmlScript())
                .techniques(command.techniques())
                .xpReward(command.xpReward())
                .expectedResult(new ExpectedTuple(command.expectedResult()))
                .ordered(command.ordered())
                .theme(command.theme())
                .difficulty(command.difficulty())
                .scenarioId(scenarioId)
                .orderIndex(orderIndex)
                .scenarioTitle(null)
                .build();

        return missionRepository.save(mission);
    }

    @Override
    public Mission update(UpdateMissionCommand command) {
        validateScenarioConstraint(command.scenarioId(), command.orderIndex());
        Mission existing = missionRepository.findById(command.id())
                .orElseThrow(() -> new MissionNotFoundException(command.id()));

        Mission updated = Mission.builder()
                .id(command.id())
                .title(command.title())
                .briefing(command.briefing())
                .objective(command.objective())
                .hint(command.hint())
                .ddlScript(command.ddlScript())
                .dmlScript(command.dmlScript())
                .techniques(command.techniques())
                .xpReward(command.xpReward())
                .expectedResult(new ExpectedTuple(command.expectedResult()))
                .ordered(command.ordered())
                .theme(command.theme())
                .difficulty(command.difficulty())
                .scenarioId(command.scenarioId())
                .orderIndex(command.orderIndex())
                .scenarioTitle(existing.getScenarioTitle())
                .build();

        return missionRepository.save(updated);
    }

    @Override
    public void delete(UUID missionId) {
        if (missionRepository.findById(missionId).isEmpty()) {
            throw new MissionNotFoundException(missionId);
        }
        missionRepository.deleteById(missionId);
    }

    @Override
    @Transactional(readOnly = true)
    public Mission findById(UUID missionId) {
        return missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionNotFoundException(missionId));
    }
}
