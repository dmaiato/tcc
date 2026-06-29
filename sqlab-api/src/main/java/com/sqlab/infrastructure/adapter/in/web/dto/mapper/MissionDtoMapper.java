package com.sqlab.infrastructure.adapter.in.web.dto.mapper;

import com.sqlab.application.port.in.GetAdminMissionsUseCase;
import com.sqlab.application.port.in.GetMissionsUseCase;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.Page;
import com.sqlab.infrastructure.adapter.in.web.dto.MissionDto;

import java.util.List;
import java.util.stream.Collectors;

public class MissionDtoMapper {

    public static MissionDto.MissionSummary toSummary(Mission mission, String scenarioTitle) {
        return new MissionDto.MissionSummary(
                mission.getId(), mission.getTitle(), scenarioTitle,
                TechniqueDtoMapper.toResponseList(mission.getTechniques()),
                mission.getXpReward(), mission.isOrdered(), mission.getDifficulty(),
                mission.getScenarioId(), mission.isEnabled(), mission.getRequiredLevel(),
                ThemeDtoMapper.toResponse(mission.getTheme()));
    }

    public static MissionDto.MissionResponse toResponse(GetMissionsUseCase.MissionDetail detail) {
        Mission m = detail.mission();
        return new MissionDto.MissionResponse(
                m.getId(), m.getTitle(), m.getBriefing(),
                m.getObjective(), m.getHint(),
                m.getDdlScript(), m.getDmlScript(),
                TechniqueDtoMapper.toResponseList(m.getTechniques()),
                m.getXpReward(), m.isOrdered(), m.getDifficulty(),
                m.getScenarioId(), detail.scenarioTitle(), m.getOrderIndex(),
                detail.scenarioTotalMissions(), m.isEnabled(), null, m.getRequiredLevel(),
                ThemeDtoMapper.toResponse(m.getTheme()));
    }

    public static MissionDto.MissionResponse toMissionResponse(GetAdminMissionsUseCase.AdminMissionResult result) {
        Mission m = result.mission();
        return new MissionDto.MissionResponse(
                m.getId(), m.getTitle(), m.getBriefing(),
                m.getObjective(), m.getHint(),
                m.getDdlScript(), m.getDmlScript(),
                TechniqueDtoMapper.toResponseList(m.getTechniques()),
                m.getXpReward(), m.isOrdered(), m.getDifficulty(),
                m.getScenarioId(), result.scenarioTitle(), m.getOrderIndex(),
                result.scenarioTotalMissions(), m.isEnabled(), m.getExpectedResult().rows(), m.getRequiredLevel(),
                ThemeDtoMapper.toResponse(m.getTheme()));
    }

    public static MissionDto.MissionPage toPage(Page<Mission> page) {
        List<MissionDto.MissionSummary> content = page.content().stream()
                .map(m -> toSummary(m, null))
                .toList();
        return new MissionDto.MissionPage(content, page.totalElements(), page.totalPages(),
                page.number(), page.size(), page.hasNext());
    }

    public static MissionDto.AdminMissionPage toAdminPage(Page<GetAdminMissionsUseCase.AdminMissionResult> page) {
        List<MissionDto.MissionResponse> content = page.content().stream()
                .map(MissionDtoMapper::toMissionResponse)
                .collect(Collectors.toList());
        return new MissionDto.AdminMissionPage(content, page.totalElements(), page.totalPages(),
                page.number(), page.size(), page.hasNext());
    }

    public static MissionDto.ValidationResponse toValidationResponse(
            com.sqlab.domain.model.ValidationResult result) {
        return new MissionDto.ValidationResponse(result.correct(), result.feedback());
    }

    private MissionDtoMapper() {
    }
}
