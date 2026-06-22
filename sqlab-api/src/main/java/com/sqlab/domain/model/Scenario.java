package com.sqlab.domain.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Scenario {
    private final UUID id;
    private String title;
    private String description;
    private Theme theme;
    private int requiredLevel;
    private boolean enabled;

    public void disable() { this.enabled = false; }
    public void enable() { this.enabled = true; }
}
