package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetUserSkillsUseCase;
import com.sqlab.application.port.out.MissionQueryPort;
import com.sqlab.application.port.out.ProgressRepository;
import com.sqlab.domain.model.Technique;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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

    @SuppressWarnings("null")
    @Override
    public List<String> handle(Query query) {
        final Set<UUID> completedMissionIds = progressRepository.findCompletedMissionIdsByUserId(query.userId());

        if (completedMissionIds.isEmpty()) {
            return List.of();
        }

        final Set<String> skills = new TreeSet<>(); // already ordered (will insert in order)

        missionQueryPort.findAllById(completedMissionIds)
                .forEach(mission -> skills.addAll(
                    mission.getTechniques().stream()
                        .map(Technique::name).collect(Collectors.toSet())));

        return List.copyOf(skills);
    }
}
