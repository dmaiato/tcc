package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetUserProgressUseCase;
import com.sqlab.application.port.out.MissionQueryPort;
import com.sqlab.application.port.out.ProgressRepository;
import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Progress;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetUserProgressService implements GetUserProgressUseCase {

    private final ProgressRepository progressRepository;
    private final MissionQueryPort missionQueryPort;
    private final ScenarioRepository scenarioRepository;

    public GetUserProgressService(ProgressRepository progressRepository,
                                  MissionQueryPort missionQueryPort,
                                  ScenarioRepository scenarioRepository) {
        this.progressRepository = progressRepository;
        this.missionQueryPort = missionQueryPort;
        this.scenarioRepository = scenarioRepository;
    }

    @Override
    public List<ProgressItem> handle(Query query) {
        List<Progress> progressList = progressRepository.findByUserId(query.userId());

        if (progressList.isEmpty()) {
            return List.of();
        }

        final Set<UUID> missionIds = progressList.stream()
                .map(Progress::getMissionId)
                .collect(Collectors.toSet());

        final Map<UUID, Mission> missionMap = missionQueryPort.findAllById(missionIds).stream()
                .collect(Collectors.toMap(Mission::getId, Function.identity()));

        final Set<UUID> scenarioIds = missionMap.values().stream()
                .map(Mission::getScenarioId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        final Map<UUID, String> scenarioTitles = new HashMap<>();
        scenarioRepository.findAllById(scenarioIds)
                .forEach(s -> scenarioTitles.put(s.getId(), s.getTitle()));

        return progressList.stream()
                .map(p -> {
                    Mission m = missionMap.get(p.getMissionId());
                    return new ProgressItem(
                            p.getMissionId(), p.isCompleted(), p.getCompletedAt(),
                            m != null ? m.getTitle() : null,
                            m != null ? m.getScenarioId() : null,
                            m != null ? scenarioTitles.get(m.getScenarioId()) : null);
                })
                .toList();
    }
}