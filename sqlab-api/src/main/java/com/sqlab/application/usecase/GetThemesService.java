package com.sqlab.application.usecase;

import com.sqlab.application.port.in.GetThemesUseCase;
import com.sqlab.application.port.out.ThemeRepository;
import com.sqlab.domain.model.Theme;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetThemesService implements GetThemesUseCase {

    private final ThemeRepository themeRepository;

    public GetThemesService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    @Override
    public List<Theme> getAllThemes() {
        return themeRepository.findAll();
    }
}
