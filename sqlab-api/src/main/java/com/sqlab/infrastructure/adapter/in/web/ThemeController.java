package com.sqlab.infrastructure.adapter.in.web;

import com.sqlab.application.port.in.GetThemesUseCase;
import com.sqlab.infrastructure.adapter.in.web.dto.ThemeDto;
import com.sqlab.infrastructure.adapter.in.web.dto.mapper.ThemeDtoMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ThemeController {

    private final GetThemesUseCase getThemesUseCase;

    public ThemeController(GetThemesUseCase getThemesUseCase) {
        this.getThemesUseCase = getThemesUseCase;
    }

    @GetMapping("/themes")
    public ResponseEntity<List<ThemeDto.ThemeResponse>> getAllThemes() {
        List<ThemeDto.ThemeResponse> response = getThemesUseCase.getAllThemes().stream()
                .map(ThemeDtoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
}
