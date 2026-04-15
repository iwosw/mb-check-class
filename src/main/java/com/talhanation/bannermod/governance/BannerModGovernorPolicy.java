package com.talhanation.bannermod.governance;

public enum BannerModGovernorPolicy {
    GARRISON_PRIORITY("garrison priority"),
    FORTIFICATION_PRIORITY("fortification priority"),
    TAX_PRESSURE("tax pressure");

    public static final int MIN_VALUE = 0;
    public static final int DEFAULT_VALUE = 1;
    public static final int MAX_VALUE = 2;

    private final String displayName;

    BannerModGovernorPolicy(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return this.displayName;
    }

    public int clamp(int value) {
        return Math.max(MIN_VALUE, Math.min(MAX_VALUE, value));
    }

    public String valueLabel(int value) {
        return switch (clamp(value)) {
            case MIN_VALUE -> "low";
            case MAX_VALUE -> "high";
            default -> "balanced";
        };
    }
}
