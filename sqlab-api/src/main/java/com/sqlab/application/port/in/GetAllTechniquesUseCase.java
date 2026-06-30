package com.sqlab.application.port.in;

import com.sqlab.domain.model.Technique;

import java.util.List;

public interface GetAllTechniquesUseCase {
    List<Technique> getAll();
}
