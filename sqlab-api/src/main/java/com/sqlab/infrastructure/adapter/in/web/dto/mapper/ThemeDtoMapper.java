package com.sqlab.infrastructure.adapter.in.web.dto.mapper;

import com.sqlab.domain.model.Theme;
import com.sqlab.infrastructure.adapter.in.web.dto.ThemeDto;

public class ThemeDtoMapper {
    public static ThemeDto.ThemeResponse toResponse(Theme theme) {
        return new ThemeDto.ThemeResponse(
                theme.getId(), theme.getName(),
                theme.getDescription(), theme.getEmoji());
    }
}
