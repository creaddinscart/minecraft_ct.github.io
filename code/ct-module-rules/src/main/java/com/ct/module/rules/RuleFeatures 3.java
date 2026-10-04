package com.ct.module.rules;

public record RuleFeatures(boolean demoUser, boolean customResolution, boolean quickPlaysSupport,
        boolean quickPlaySingleplayer, boolean quickPlayMultiplayer, boolean quickPlayRealms) {

    public static RuleFeatures none() {
        return new RuleFeatures(false, false, false, false, false, false);
    }

    public boolean enabled(String key) {
        return switch (key) {
            case "is_demo_user" -> demoUser;
            case "has_custom_resolution" -> customResolution;
            case "has_quick_plays_support" -> quickPlaysSupport;
            case "is_quick_play_singleplayer" -> quickPlaySingleplayer;
            case "is_quick_play_multiplayer" -> quickPlayMultiplayer;
            case "is_quick_play_realms" -> quickPlayRealms;
            default -> false;
        };
    }
}
