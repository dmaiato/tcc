package com.sqlab.application.port.in;

import com.sqlab.domain.model.Scenario;

import java.util.List;
import java.util.UUID;

public interface GetScenariosUseCase {
    List<Scenario> handle();
    Scenario handle(UUID id);
}
