package com.sqlab.infrastructure.adapter.in.web.dto.mapper;

import com.sqlab.domain.model.Technique;
import com.sqlab.infrastructure.adapter.in.web.dto.TechniqueDto;

import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.NonNull;

public class TechniqueDtoMapper {
    public static TechniqueDto.TechniqueResponse toResponse(@NonNull Technique technique) {
        return new TechniqueDto.TechniqueResponse(technique.id(), technique.name());
    }

    public static List<TechniqueDto.TechniqueResponse> toResponseList(@NonNull Collection<Technique> techniques) {
        // if (techniques == null) return List.of();
        return techniques.stream()
                .map(TechniqueDtoMapper::toResponse)
                .toList();
    }
}
