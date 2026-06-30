package com.sqlab.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class TechniqueDto {
    public record TechniqueResponse(UUID id, String name) {}

    public record CreateTechniqueRequest(@NotBlank String name) {}

    public record UpdateTechniqueRequest(@NotBlank String name) {}
}
