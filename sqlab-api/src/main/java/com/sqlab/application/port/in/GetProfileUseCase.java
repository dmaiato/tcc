package com.sqlab.application.port.in;

import com.sqlab.domain.model.User;

import java.util.List;
import java.util.UUID;

public interface GetProfileUseCase {

    record Query(UUID userId) {}

    record ProfileResponse(User user, List<GetUserProgressUseCase.ProgressItem> progress, List<String> skills) {}

    ProfileResponse handle(Query query);
}
