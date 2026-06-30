package com.sqlab.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class ThemeDto {

    public record ThemeResponse(UUID id, String name, String description, String emoji) {}

    public record CreateThemeRequest(@NotBlank String name, String description, String emoji) {}

    public record UpdateThemeRequest(@NotBlank String name, String description, String emoji) {}
}
