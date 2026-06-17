package com.sqlab.infrastructure.adapter.in.web.dto.mapper;

import com.sqlab.application.port.in.GetAdminScenariosUseCase;
import com.sqlab.application.port.in.GetScenariosUseCase;
import com.sqlab.infrastructure.adapter.in.web.dto.ScenarioDto;

import java.util.List;
import java.util.Map;

public class ScenarioDtoMapper {

    public static ScenarioDto.ScenarioSummary toSummary(GetScenariosUseCase.ScenarioSummaryResult result) {
        return new ScenarioDto.ScenarioSummary(
                result.id(), result.title(), result.totalMissions(),
                result.completedMissions(), result.requiredLevel(),
                ThemeDtoMapper.toResponse(result.theme()));
    }

    public static List<ScenarioDto.ScenarioMissionItem> toMissionItems(
            List<GetScenariosUseCase.MissionStatus> missions) {
        return missions.stream()
                .map(ms -> new ScenarioDto.ScenarioMissionItem(
                        ms.mission().getId(), ms.mission().getTitle(),
                        TechniqueDtoMapper.toResponseList(ms.mission().getTechniques()),
                        ms.mission().getXpReward(), ms.mission().getDifficulty(),
                        ms.status(), ms.mission().getRequiredLevel()))
                .toList();
    }

    public static ScenarioDto.ScenarioDetail toDetail(GetScenariosUseCase.ScenarioDetailResult detail,
                                                      List<ScenarioDto.ScenarioMissionItem> missionItems) {
        return new ScenarioDto.ScenarioDetail(
                detail.scenario().getId(), detail.scenario().getTitle(),
                detail.scenario().getDescription(), missionItems,
                Map.of("completedCount", detail.completedCount(), "totalCount", detail.totalCount()),
                detail.scenario().getRequiredLevel(),
                ThemeDtoMapper.toResponse(detail.scenario().getTheme()));
    }

    public static ScenarioDto.ScenarioResponse toResponse(GetAdminScenariosUseCase.ScenarioListResult result) {
        return new ScenarioDto.ScenarioResponse(
                result.scenario().getId(), result.scenario().getTitle(),
                result.scenario().getDescription(), result.totalMissions(),
                result.scenario().isEnabled(), result.scenario().getRequiredLevel(),
                ThemeDtoMapper.toResponse(result.scenario().getTheme()));
    }

    public static ScenarioDto.ScenarioResponse toResponse(
            com.sqlab.domain.model.Scenario scenario, int totalMissions) {
        return new ScenarioDto.ScenarioResponse(
                scenario.getId(), scenario.getTitle(), scenario.getDescription(),
                totalMissions, scenario.isEnabled(), scenario.getRequiredLevel(),
                ThemeDtoMapper.toResponse(scenario.getTheme()));
    }

    public static List<ScenarioDto.ScenarioMissionSummary> toMissionSummaries(
            List<com.sqlab.domain.model.Mission> missions) {
        return missions.stream()
                .map(m -> new ScenarioDto.ScenarioMissionSummary(
                        m.getId(), m.getTitle(), m.getDifficulty(), m.getXpReward(), m.isEnabled()))
                .toList();
    }

    public static ScenarioDto.ScenarioAdminDetail toAdminDetail(
            GetAdminScenariosUseCase.ScenarioDetailResult detail,
            List<ScenarioDto.ScenarioMissionSummary> missionSummaries) {
        return new ScenarioDto.ScenarioAdminDetail(
                detail.scenario().getId(), detail.scenario().getTitle(),
                detail.scenario().getDescription(), detail.scenario().isEnabled(),
                missionSummaries.size(), missionSummaries, detail.scenario().getRequiredLevel(),
                ThemeDtoMapper.toResponse(detail.scenario().getTheme()));
    }

    private ScenarioDtoMapper() {
    }
}
