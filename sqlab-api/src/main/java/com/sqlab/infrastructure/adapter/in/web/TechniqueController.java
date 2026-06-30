package com.sqlab.infrastructure.adapter.in.web;

import com.sqlab.application.port.in.GetAllTechniquesUseCase;
import com.sqlab.application.port.in.ManageTechniquesUseCase;
import com.sqlab.infrastructure.adapter.in.web.dto.TechniqueDto;
import com.sqlab.infrastructure.adapter.in.web.dto.mapper.TechniqueDtoMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class TechniqueController {

    private final GetAllTechniquesUseCase getAllTechniquesUseCase;
    private final ManageTechniquesUseCase manageTechniquesUseCase;

    public TechniqueController(GetAllTechniquesUseCase getAllTechniquesUseCase,
                               ManageTechniquesUseCase manageTechniquesUseCase) {
        this.getAllTechniquesUseCase = getAllTechniquesUseCase;
        this.manageTechniquesUseCase = manageTechniquesUseCase;
    }

    @GetMapping("/techniques")
    public ResponseEntity<List<TechniqueDto.TechniqueResponse>> getAll() {
        var response = TechniqueDtoMapper.toResponseList(getAllTechniquesUseCase.getAll());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/techniques")
    public ResponseEntity<List<TechniqueDto.TechniqueResponse>> getAllAdmin() {
        var response = TechniqueDtoMapper.toResponseList(getAllTechniquesUseCase.getAll());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/techniques")
    public ResponseEntity<TechniqueDto.TechniqueResponse> create(
            @Valid @RequestBody TechniqueDto.CreateTechniqueRequest request) {
        var command = new ManageTechniquesUseCase.CreateTechniqueCommand(request.name());
        var technique = manageTechniquesUseCase.create(command);
        var response = TechniqueDtoMapper.toResponse(technique);
        return ResponseEntity.created(URI.create("/api/admin/techniques/" + technique.getId()))
                .body(response);
    }

    @PutMapping("/admin/techniques/{id}")
    public ResponseEntity<TechniqueDto.TechniqueResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody TechniqueDto.UpdateTechniqueRequest request) {
        var command = new ManageTechniquesUseCase.UpdateTechniqueCommand(id, request.name());
        var technique = manageTechniquesUseCase.update(command);
        return ResponseEntity.ok(TechniqueDtoMapper.toResponse(technique));
    }

    @DeleteMapping("/admin/techniques/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        manageTechniquesUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
