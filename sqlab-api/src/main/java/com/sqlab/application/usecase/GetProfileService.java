package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetProfileUseCase;
import com.sqlab.application.port.in.GetUserProgressUseCase;
import com.sqlab.application.port.in.GetUserSkillsUseCase;
import com.sqlab.application.port.out.UserRepository;
import com.sqlab.domain.exception.UserNotFoundException;
import com.sqlab.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetProfileService implements GetProfileUseCase {

    private final UserRepository userRepository;
    private final GetUserProgressUseCase getUserProgressUseCase;
    private final GetUserSkillsUseCase getUserSkillsUseCase;

    public GetProfileService(UserRepository userRepository,
                             GetUserProgressUseCase getUserProgressUseCase,
                             GetUserSkillsUseCase getUserSkillsUseCase) {
        this.userRepository = userRepository;
        this.getUserProgressUseCase = getUserProgressUseCase;
        this.getUserSkillsUseCase = getUserSkillsUseCase;
    }

    @Override
    public ProfileResponse handle(Query query) {
        User user = userRepository.findById(query.userId())
                .orElseThrow(() -> new UserNotFoundException(query.userId()));
        var progress = getUserProgressUseCase.handle(new GetUserProgressUseCase.Query(query.userId()));
        var skills = getUserSkillsUseCase.handle(new GetUserSkillsUseCase.Query(query.userId()));
        return new ProfileResponse(user, progress, skills);
    }
}
