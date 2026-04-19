package com.talhanation.bannermod.settlement;

public enum BannerModSettlementBuildingCategory {
    FOOD,
    MATERIAL,
    STORAGE,
    MARKET,
    CONSTRUCTION,
    GENERAL;

    public static BannerModSettlementBuildingCategory fromTagName(String name) {
        if (name == null || name.isBlank()) {
            return GENERAL;
        }
        return BannerModSettlementBuildingCategory.valueOf(name);
    }
}
