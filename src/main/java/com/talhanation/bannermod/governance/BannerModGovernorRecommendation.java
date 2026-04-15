package com.talhanation.bannermod.governance;

public enum BannerModGovernorRecommendation {
    HOLD_COURSE("hold_course"),
    INCREASE_GARRISON("increase_garrison"),
    STRENGTHEN_FORTIFICATIONS("strengthen_fortifications"),
    RELIEVE_SUPPLY_PRESSURE("relieve_supply_pressure");

    private final String token;

    BannerModGovernorRecommendation(String token) {
        this.token = token;
    }

    public String token() {
        return this.token;
    }
}
