package com.sqlab.application.port.in;

import com.sqlab.domain.model.Technique;

import java.util.UUID;

public interface ManageTechniquesUseCase {

    record CreateTechniqueCommand(String name) {}

    record UpdateTechniqueCommand(UUID id, String name) {}

    Technique create(CreateTechniqueCommand command);

    Technique update(UpdateTechniqueCommand command);

    void delete(UUID id);
}
