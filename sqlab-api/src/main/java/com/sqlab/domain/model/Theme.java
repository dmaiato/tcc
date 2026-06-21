package com.sqlab.domain.model;

import java.util.UUID;

public class Theme {
    private final UUID id;
    private final String name;
    private String description;
    private String emoji;

    public Theme(UUID id, String name, String description, String emoji) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.emoji = emoji;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getEmoji() { return emoji; }

    public void setDescription(String description) { this.description = description; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Theme theme)) return false;
        // equals/hashCode based on name (business key) — id is NOT considered
        return name.equals(theme.name);
    }

    @Override
    public int hashCode() {
        // equals/hashCode based on name (business key) — id is NOT considered
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Theme{name='" + name + "'}";
    }
}
