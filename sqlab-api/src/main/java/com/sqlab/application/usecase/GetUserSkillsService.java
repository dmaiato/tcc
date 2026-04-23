package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetUserSkillsUseCase;
import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.application.port.out.ProgressRepository;
import com.sqlab.domain.model.Progress;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetUserSkillsService implements GetUserSkillsUseCase {

    private final ProgressRepository progressRepository;
    private final MissionRepository missionRepository;

    public GetUserSkillsService(ProgressRepository progressRepository,
                             MissionRepository missionRepository) {
        this.progressRepository = progressRepository;
        this.missionRepository = missionRepository;
    }

    @Override
    public List<String> handle(Query query) {
        List<Progress> completedProgress = progressRepository.findCompletedByUserId(query.userId());

        Set<String> skills = new HashSet<>();
        for (Progress progress : completedProgress) {
            missionRepository.findById(progress.getMissionId())
                    .ifPresent(mission -> skills.addAll(mission.getTechniques()));
        }

        return skills.stream().sorted().toList();
    }
}