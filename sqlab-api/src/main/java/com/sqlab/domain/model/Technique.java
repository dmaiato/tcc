package com.sqlab.domain.model;

import java.util.Comparator;
import java.util.UUID;

public record Technique(UUID id, String name) {

    @SuppressWarnings("null")
    public static final Comparator<Technique> ALPHABETICAL = Comparator.comparing(Technique::name);
}
