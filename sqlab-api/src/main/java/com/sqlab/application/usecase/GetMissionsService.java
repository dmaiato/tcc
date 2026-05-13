package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetMissionsUseCase;
import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.domain.exception.MissionLockedException;
import com.sqlab.domain.exception.MissionNotFoundException;
import com.sqlab.domain.model.Mission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetMissionsService implements GetMissionsUseCase {

    private final MissionRepository missionRepository;

    public GetMissionsService(MissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    @Override
    public List<Mission> handle(ListAllQuery query) {
        if (query.theme() != null && query.difficulty() != null) {
            return missionRepository.findByThemeAndDifficulty(query.theme(), query.difficulty());
        }
        if (query.theme() != null) {
            return missionRepository.findByTheme(query.theme());
        }
        if (query.difficulty() != null) {
            return missionRepository.findByDifficulty(query.difficulty());
        }
        return missionRepository.findAll();
    }

    @Override
    public Mission handle(FindByIdQuery query) {
        Mission mission = missionRepository.findById(query.missionId())
                .orElseThrow(() -> new MissionNotFoundException(query.missionId()));

        if (query.userId() != null && mission.getScenarioId() != null) {
            if (mission.getOrderIndex() != null && mission.getOrderIndex() > 1) {
                boolean prevCompleted = missionRepository.isPreviousMissionCompleted(
                        query.userId(), mission.getScenarioId(), mission.getOrderIndex() - 1);
                if (!prevCompleted) {
                    throw new MissionLockedException(
                            mission.getId(), mission.getScenarioId(), mission.getScenarioTitle());
                }
            }
        }

        return mission;
    }

    @Override
    public MissionDetail handleDetail(FindByIdQuery query) {
        Mission mission = handle(query);
        Integer scenarioTotalMissions = null;
        if (mission.getScenarioId() != null) {
            scenarioTotalMissions = missionRepository.countByScenarioId(mission.getScenarioId());
        }
        return new MissionDetail(mission, scenarioTotalMissions);
    }
}