package com.sqlab.application.port.in;

import java.util.List;
import java.util.UUID;

public interface GetUserSkillsUseCase {

    record Query(UUID userId) {}

    List<String> handle(Query query);
}