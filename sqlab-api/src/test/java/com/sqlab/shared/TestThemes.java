package com.sqlab.shared;

import com.sqlab.domain.model.Theme;

import java.util.UUID;

public final class TestThemes {

    private static final UUID ASTRONOMY_ID = UUID.fromString("b0000001-0000-0000-0000-000000000001");
    private static final UUID CRIMINAL_ID = UUID.fromString("b0000003-0000-0000-0000-000000000003");
    private static final UUID FINANCE_ID = UUID.fromString("b0000005-0000-0000-0000-000000000005");

    public static Theme astronomy() {
        return new Theme(ASTRONOMY_ID, "ASTRONOMY", null, null);
    }

    public static Theme criminal() {
        return new Theme(CRIMINAL_ID, "CRIMINAL", null, null);
    }

    public static Theme finance() {
        return new Theme(FINANCE_ID, "FINANCE", null, null);
    }

    public static Theme named(String name) {
        return new Theme(UUID.randomUUID(), name, null, null);
    }

    private TestThemes() {
    }
}
