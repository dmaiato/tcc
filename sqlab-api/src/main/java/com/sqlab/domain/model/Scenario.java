package com.sqlab.domain.model;

import java.util.UUID;

public class Scenario {
    private final UUID id;
    private final String title;
    private final String description;
    private final Theme theme;
    private final boolean enabled;
    private final int requiredLevel;

    public Scenario(UUID id, String title, String description, Theme theme, boolean enabled, int requiredLevel) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.theme = theme;
        this.enabled = enabled;
        this.requiredLevel = requiredLevel;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Theme getTheme() { return theme; }
    public boolean isEnabled() { return enabled; }
    public int getRequiredLevel() { return requiredLevel; }
}
