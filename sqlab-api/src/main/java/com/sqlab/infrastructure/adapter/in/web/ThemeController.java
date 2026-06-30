package com.sqlab.infrastructure.adapter.in.web;

import com.sqlab.application.port.in.GetThemesUseCase;
import com.sqlab.application.port.in.ManageThemesUseCase;
import com.sqlab.infrastructure.adapter.in.web.dto.ThemeDto;
import com.sqlab.infrastructure.adapter.in.web.dto.mapper.ThemeDtoMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ThemeController {

    private final GetThemesUseCase getThemesUseCase;
    private final ManageThemesUseCase manageThemesUseCase;

    public ThemeController(GetThemesUseCase getThemesUseCase,
                           ManageThemesUseCase manageThemesUseCase) {
        this.getThemesUseCase = getThemesUseCase;
        this.manageThemesUseCase = manageThemesUseCase;
    }

    @GetMapping("/themes")
    public ResponseEntity<List<ThemeDto.ThemeResponse>> getAllThemes() {
        List<ThemeDto.ThemeResponse> response = getThemesUseCase.getAllThemes().stream()
                .map(ThemeDtoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/themes")
    public ResponseEntity<ThemeDto.ThemeResponse> create(
            @Valid @RequestBody ThemeDto.CreateThemeRequest request) {
        var command = new ManageThemesUseCase.CreateThemeCommand(
                request.name(), request.description(), request.emoji());
        var theme = manageThemesUseCase.create(command);
        var response = ThemeDtoMapper.toResponse(theme);
        return ResponseEntity.created(URI.create("/api/admin/themes/" + theme.getId()))
                .body(response);
    }

    @PutMapping("/admin/themes/{id}")
    public ResponseEntity<ThemeDto.ThemeResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ThemeDto.UpdateThemeRequest request) {
        var command = new ManageThemesUseCase.UpdateThemeCommand(
                id, request.name(), request.description(), request.emoji());
        var theme = manageThemesUseCase.update(command);
        return ResponseEntity.ok(ThemeDtoMapper.toResponse(theme));
    }

    @DeleteMapping("/admin/themes/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        manageThemesUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
