package com.talhanation.bannermod.settlement;

public enum BannerModSettlementResidentSchedulePolicySeed {
    VILLAGE_LIFE_FLEX,
    GOVERNANCE_CIVIC,
    LOCAL_LABOR_DAY,
    FLOATING_LABOR_FLEX,
    ORPHANED_LABOR_DAY;

    public static BannerModSettlementResidentSchedulePolicySeed fromTagName(String name) {
        try {
            return BannerModSettlementResidentSchedulePolicySeed.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return VILLAGE_LIFE_FLEX;
        }
    }
}
