package com.peakhitboxes;

import net.minecraftforge.common.ForgeConfigSpec;

public final class HitboxConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED, HIDE_VANILLA, LIVING_ONLY, SHOW_SELF,
            SHOW_EYE_LINE, SHOW_LOOK_LINE, USE_RANGE_COLOR, USE_HIT_COLOR, THROUGH_WALLS;
    public static final ForgeConfigSpec.DoubleValue LINE_WIDTH, RANGE, MAX_DISTANCE, LOOK_LENGTH;
    public static final ForgeConfigSpec.ConfigValue<String> BASE_COLOR, RANGE_COLOR, HIT_COLOR, EYE_COLOR, LOOK_COLOR,
            TARGET_COLOR, PLAYER_COLOR, HOSTILE_COLOR, PASSIVE_COLOR, ELYTRA_GOOD_COLOR, ELYTRA_BAD_COLOR, PVP_TEXT_COLOR;
    public static final ForgeConfigSpec.BooleanValue USE_TARGET_COLOR, USE_TYPE_COLORS, USE_TEAM_COLORS, FADE_WITH_DISTANCE,
            ELYTRA_ENABLED, ELYTRA_ICON, ELYTRA_SHOW_SPEED, PVP_ENABLED, PVP_COMBO, PVP_REACH, PVP_CPS, PVP_TARGET_HUD;
    public static final ForgeConfigSpec.DoubleValue ELYTRA_GOOD_MIN, ELYTRA_GOOD_MAX, ELYTRA_LOCK_ANGLE;
    public static final ForgeConfigSpec.IntValue ELYTRA_X, ELYTRA_Y, PVP_X, PVP_Y;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("general");
        ENABLED = b.comment("Draw Peak Hitboxes").define("enabled", true);
        HIDE_VANILLA = b.comment("Turn off vanilla F3+B boxes so they don't overlap").define("hideVanilla", true);
        LIVING_ONLY = b.comment("Only mobs/players (no items, arrows, etc.)").define("livingOnly", false);
        SHOW_SELF = b.comment("Draw your own hitbox in 3rd person").define("showSelf", false);
        THROUGH_WALLS = b.comment("Draw boxes through blocks").define("throughWalls", false);
        MAX_DISTANCE = b.comment("Don't draw boxes further than this (blocks)").defineInRange("maxDistance", 64.0, 4.0, 256.0);
        LINE_WIDTH = b.comment("Line thickness").defineInRange("lineWidth", 2.0, 0.5, 10.0);
        b.pop().push("colors");
        BASE_COLOR = b.comment("Normal box color, #AARRGGBB or #RRGGBB").define("baseColor", "#FFFFFFFF");
        USE_RANGE_COLOR = b.define("useRangeColor", true);
        RANGE_COLOR = b.comment("Color when the entity is within your attack range").define("rangeColor", "#FFFF2020");
        RANGE = b.comment("Attack range in blocks (vanilla is 3.0)").defineInRange("range", 3.0, 0.5, 8.0);
        USE_HIT_COLOR = b.define("useHitColor", true);
        HIT_COLOR = b.comment("Color while the entity is taking damage (hurt flash)").define("hitColor", "#FFFFD700");
        b.pop().push("extras");
        SHOW_EYE_LINE = b.define("showEyeLine", true);
        EYE_COLOR = b.define("eyeColor", "#FFFF4040");
        SHOW_LOOK_LINE = b.define("showLookLine", true);
        LOOK_COLOR = b.define("lookColor", "#FF4080FF");
        LOOK_LENGTH = b.defineInRange("lookLength", 2.0, 0.5, 8.0);
        b.pop().push("smartColors");
        USE_TARGET_COLOR = b.comment("Different color for the entity under your crosshair").define("useTargetColor", true);
        TARGET_COLOR = b.define("targetColor", "#FF00FF80");
        USE_TYPE_COLORS = b.comment("Base color per type instead of one base color").define("useTypeColors", false);
        PLAYER_COLOR = b.define("playerColor", "#FFFFFFFF");
        HOSTILE_COLOR = b.define("hostileColor", "#FFFF9040");
        PASSIVE_COLOR = b.define("passiveColor", "#FF80FF80");
        USE_TEAM_COLORS = b.comment("Players use their scoreboard team color").define("useTeamColors", false);
        FADE_WITH_DISTANCE = b.comment("Boxes fade out the further away they are").define("fadeWithDistance", false);
        b.pop().push("elytraPitch");
        ELYTRA_ENABLED = b.define("enabled", true);
        ELYTRA_ICON = b.define("showIcon", true);
        ELYTRA_SHOW_SPEED = b.comment("Show flight speed in blocks/sec").define("showSpeed", true);
        ELYTRA_GOOD_MIN = b.comment("Pitch range shown as 'good' (degrees, + = up)").defineInRange("goodMin", 35.0, -90.0, 90.0);
        ELYTRA_GOOD_MAX = b.defineInRange("goodMax", 45.0, -90.0, 90.0);
        ELYTRA_GOOD_COLOR = b.define("goodColor", "#FF55FF55");
        ELYTRA_BAD_COLOR = b.define("normalColor", "#FFFFFFFF");
        ELYTRA_LOCK_ANGLE = b.comment("Angle the Pitch Snap key sets you to").defineInRange("snapAngle", 40.0, -90.0, 90.0);
        ELYTRA_X = b.comment("Offset from screen center").defineInRange("offsetX", 0, -2000, 2000);
        ELYTRA_Y = b.defineInRange("offsetY", 20, -2000, 2000);
        b.pop().push("pvpHud");
        PVP_ENABLED = b.define("enabled", true);
        PVP_COMBO = b.define("showCombo", true);
        PVP_REACH = b.define("showReach", true);
        PVP_CPS = b.define("showCps", true);
        PVP_TARGET_HUD = b.comment("Name/health/armor of who you last hit").define("showTargetHud", true);
        PVP_TEXT_COLOR = b.define("textColor", "#FFFFFFFF");
        PVP_X = b.defineInRange("x", 4, 0, 4000);
        PVP_Y = b.defineInRange("y", 4, 0, 4000);
        b.pop();
        SPEC = b.build();
    }

    /** Parses #RRGGBB / #AARRGGBB to ARGB int; falls back to white on garbage. */
    public static int parseColor(String s) {
        if (s == null) return 0xFFFFFFFF;
        String h = s.trim().replace("#", "").replace("0x", "");
        try {
            if (h.length() == 6) return 0xFF000000 | Integer.parseInt(h, 16);
            if (h.length() == 8) return (int) Long.parseLong(h, 16);
        } catch (NumberFormatException ignored) { }
        return 0xFFFFFFFF;
    }

    public static boolean isValidColor(String s) {
        String h = s.trim().replace("#", "");
        return (h.length() == 6 || h.length() == 8) && h.matches("[0-9a-fA-F]+");
    }

    private HitboxConfig() { }
}
