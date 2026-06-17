package com.sqlab.infrastructure.adapter.in.web.dto;

import java.util.UUID;

public class TechniqueDto {
    public record TechniqueResponse(UUID id, String name) {}
}
