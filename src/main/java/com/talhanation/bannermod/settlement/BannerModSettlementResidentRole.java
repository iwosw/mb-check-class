package com.talhanation.bannermod.settlement;

public enum BannerModSettlementResidentRole {
    VILLAGER,
    CONTROLLED_WORKER,
    GOVERNOR_RECRUIT;

    public static BannerModSettlementResidentRole fromTagName(String name) {
        if (name == null || name.isBlank()) {
            return VILLAGER;
        }
        try {
            return valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return VILLAGER;
        }
    }
}
