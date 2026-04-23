package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetUserProgressUseCase;
import com.sqlab.application.port.out.ProgressRepository;
import com.sqlab.domain.model.Progress;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetUserProgressService implements GetUserProgressUseCase {

    private final ProgressRepository progressRepository;

    public GetUserProgressService(ProgressRepository progressRepository) {
        this.progressRepository = progressRepository;
    }

    @Override
    public List<Progress> handle(Query query) {
        return progressRepository.findByUserId(query.userId());
    }
}