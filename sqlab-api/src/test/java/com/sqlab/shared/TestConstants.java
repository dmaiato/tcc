package com.sqlab.shared;

import java.util.UUID;

public final class TestConstants {

    public static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final UUID ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    public static final UUID MISSION_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    public static final UUID SCENARIO_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    public static final UUID THEME_ID = UUID.fromString("b0000001-0000-0000-0000-000000000001");

    public static final String THEME_ASTRONOMY = "ASTRONOMY";
    public static final String THEME_CRIMINAL = "CRIMINAL";
    public static final String THEME_FINANCE = "FINANCE";

    private TestConstants() {
    }
}
