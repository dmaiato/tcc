package com.sqlab.application.port.in;

import com.sqlab.domain.model.Progress;

import java.util.List;
import java.util.UUID;

public interface GetUserProgressUseCase {

    record Query(UUID userId) {}

    List<Progress> handle(Query query);
}