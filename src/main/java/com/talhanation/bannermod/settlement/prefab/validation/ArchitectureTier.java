package com.talhanation.bannermod.settlement.prefab.validation;

/**
 * Discrete reward tiers derived from the raw architecture score. Keeps rewards coarse
 * enough for players to feel meaningful differences.
 */
public enum ArchitectureTier {
    HOVEL(0, "bannermod.prefab.tier.hovel"),
    ACCEPTABLE(35, "bannermod.prefab.tier.acceptable"),
    GOOD(55, "bannermod.prefab.tier.good"),
    GREAT(75, "bannermod.prefab.tier.great"),
    MAJESTIC(90, "bannermod.prefab.tier.majestic");

    private final int minScore;
    private final String translationKey;

    ArchitectureTier(int minScore, String translationKey) {
        this.minScore = minScore;
        this.translationKey = translationKey;
    }

    public int minScore() {
        return minScore;
    }

    public String translationKey() {
        return translationKey;
    }

    public static ArchitectureTier fromScore(int score) {
        ArchitectureTier out = HOVEL;
        for (ArchitectureTier tier : values()) {
            if (score >= tier.minScore) {
                out = tier;
            }
        }
        return out;
    }
}
