package com.talhanation.bannermod.governance;

public enum BannerModGovernorIncident {
    HOSTILE_CLAIM("hostile_claim"),
    DEGRADED_SETTLEMENT("degraded_settlement"),
    UNCLAIMED_SETTLEMENT("unclaimed_settlement"),
    UNDER_SIEGE("under_siege"),
    WORKER_SHORTAGE("worker_shortage"),
    SUPPLY_BLOCKED("supply_blocked"),
    RECRUIT_UPKEEP_BLOCKED("recruit_upkeep_blocked");

    private final String token;

    BannerModGovernorIncident(String token) {
        this.token = token;
    }

    public String token() {
        return this.token;
    }
}
