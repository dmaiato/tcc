package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetMissionsUseCase;
import com.sqlab.application.port.out.MissionRepository;
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
        return missionRepository.findById(query.missionId())
                .orElseThrow(() -> new MissionNotFoundException(query.missionId()));
    }
}