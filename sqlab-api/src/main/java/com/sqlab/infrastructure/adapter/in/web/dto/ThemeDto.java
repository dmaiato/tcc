package com.sqlab.infrastructure.adapter.in.web.dto;

import java.util.UUID;

public class ThemeDto {

    public record ThemeResponse(UUID id, String name, String description, String emoji) {}
}
