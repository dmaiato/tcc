package com.sqlab.infrastructure.adapter.in.web.dto.mapper;

import com.sqlab.domain.model.Technique;
import com.sqlab.infrastructure.adapter.in.web.dto.TechniqueDto;

import java.util.List;
import java.util.UUID;

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

    public static Technique toDomain(TechniqueDto.CreateTechniqueRequest request) {
        return new Technique(null, request.name().trim());
    }

    public static Technique toDomain(TechniqueDto.CreateTechniqueRequest request, UUID id) {
        return new Technique(id, request.name().trim());
    }
}
