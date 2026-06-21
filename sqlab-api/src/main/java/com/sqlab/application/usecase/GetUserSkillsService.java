package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetUserSkillsUseCase;
import com.sqlab.application.port.out.MissionQueryPort;
import com.sqlab.application.port.out.ProgressRepository;
import com.sqlab.domain.model.Technique;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetUserSkillsService implements GetUserSkillsUseCase {

    private final ProgressRepository progressRepository;
    private final MissionQueryPort missionQueryPort;

    public GetUserSkillsService(ProgressRepository progressRepository,
                                MissionQueryPort missionQueryPort) {
        this.progressRepository = progressRepository;
        this.missionQueryPort = missionQueryPort;
    }

    @Override
    public List<String> handle(Query query) {
        Set<UUID> completedMissionIds = progressRepository.findCompletedMissionIdsByUserId(query.userId());

        if (completedMissionIds.isEmpty()) {
            return List.of();
        }

        Set<String> skills = new HashSet<>();
        missionQueryPort.findAllById(completedMissionIds)
                .forEach(mission -> skills.addAll(
                        mission.getTechniques().stream().map(Technique::getName).collect(Collectors.toSet())));

        return skills.stream().sorted().toList();
    }
}
