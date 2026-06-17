package com.sqlab.infrastructure.adapter.in.web.dto.mapper;

import com.sqlab.domain.model.Technique;
import com.sqlab.infrastructure.adapter.in.web.dto.TechniqueDto;

import java.util.List;

public class TechniqueDtoMapper {
    public static TechniqueDto.TechniqueResponse toResponse(Technique technique) {
        return new TechniqueDto.TechniqueResponse(technique.getId(), technique.getName());
    }

    public static List<TechniqueDto.TechniqueResponse> toResponseList(List<Technique> techniques) {
        if (techniques == null) return List.of();
        return techniques.stream()
                .map(TechniqueDtoMapper::toResponse)
                .toList();
    }
}
