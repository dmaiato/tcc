package com.sqlab.domain.model;

import java.util.UUID;

public class Scenario {
    private final UUID id;
    private String title;
    private String description;
    private Theme theme;
    private int requiredLevel;
    private boolean enabled;

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

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setTheme(Theme theme) { this.theme = theme; }
    public void setRequiredLevel(int requiredLevel) { this.requiredLevel = requiredLevel; }

    public void disable() { this.enabled = false; }
    public void enable() { this.enabled = true; }
}
