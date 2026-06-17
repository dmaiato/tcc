package com.sqlab.domain.model;

import java.util.UUID;

public class Technique {
    private final UUID id;
    private final String name;

    public Technique(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Technique t)) return false;
        // equals/hashCode based on name (business key) — id is NOT considered
        return name.equals(t.name);
    }

    @Override
    public int hashCode() {
        // equals/hashCode based on name (business key) — id is NOT considered
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Technique{name='" + name + "'}";
    }
}
