package com.talhanation.bannermod.settlement;

public enum BannerModSettlementJobHandlerSeed {
    NONE,
    VILLAGE_LIFE,
    GOVERNANCE,
    LOCAL_BUILDING_LABOR,
    FLOATING_LABOR_POOL,
    ORPHANED_LABOR_RECOVERY;

    public static BannerModSettlementJobHandlerSeed fromTagName(String name) {
        if (name == null || name.isBlank()) {
            return NONE;
        }
        try {
            return valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return NONE;
        }
    }
}
