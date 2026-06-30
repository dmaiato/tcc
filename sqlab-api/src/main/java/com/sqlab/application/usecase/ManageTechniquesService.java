package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetAllTechniquesUseCase;
import com.sqlab.application.port.in.ManageTechniquesUseCase;
import com.sqlab.application.port.out.MissionQueryPort;
import com.sqlab.application.port.out.TechniqueRepository;
import com.sqlab.domain.exception.TechniqueNotFoundException;
import com.sqlab.domain.model.Technique;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ManageTechniquesService implements ManageTechniquesUseCase, GetAllTechniquesUseCase {

    private final TechniqueRepository techniqueRepository;
    private final MissionQueryPort missionQueryPort;

    public ManageTechniquesService(TechniqueRepository techniqueRepository,
                                   MissionQueryPort missionQueryPort) {
        this.techniqueRepository = techniqueRepository;
        this.missionQueryPort = missionQueryPort;
    }

    @Override
    public List<Technique> getAll() {
        return techniqueRepository.findAll();
    }

    @Override
    @Transactional
    public Technique create(CreateTechniqueCommand command) {
        if (command.name() == null || command.name().isBlank()) {
            throw new IllegalArgumentException("Technique name must not be blank");
        }
        if (techniqueRepository.existsByName(command.name())) {
            throw new IllegalArgumentException("Technique already exists: " + command.name());
        }
        var technique = new Technique(null, command.name().trim());
        return techniqueRepository.save(technique);
    }

    @Override
    @Transactional
    public Technique update(UpdateTechniqueCommand command) {
        var existing = techniqueRepository.findById(command.id())
                .orElseThrow(() -> new TechniqueNotFoundException(command.id()));
        if (command.name() == null || command.name().isBlank()) {
            throw new IllegalArgumentException("Technique name must not be blank");
        }
        if (!command.name().equalsIgnoreCase(existing.getName())
                && techniqueRepository.existsByName(command.name())) {
            throw new IllegalArgumentException("Technique already exists: " + command.name());
        }
        var updated = new Technique(existing.getId(), command.name().trim());
        return techniqueRepository.save(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        var technique = techniqueRepository.findById(id)
                .orElseThrow(() -> new TechniqueNotFoundException(id));
        if (missionQueryPort.existsByTechniqueName(technique.getName())) {
            throw new IllegalStateException("Cannot delete technique '" + technique.getName()
                    + "' because it is referenced by one or more missions");
        }
        techniqueRepository.deleteById(id);
    }
}
