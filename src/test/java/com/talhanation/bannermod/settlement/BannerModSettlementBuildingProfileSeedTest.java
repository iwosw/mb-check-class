package com.talhanation.bannermod.settlement;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BannerModSettlementBuildingProfileSeedTest {

    @Test
    void categoryMatchesExpectedEnumBuckets() {
        assertEquals(BannerModSettlementBuildingCategory.FOOD, BannerModSettlementBuildingProfileSeed.FOOD_PRODUCTION.category());
        assertEquals(BannerModSettlementBuildingCategory.MATERIAL, BannerModSettlementBuildingProfileSeed.MATERIAL_PRODUCTION.category());
        assertEquals(BannerModSettlementBuildingCategory.STORAGE, BannerModSettlementBuildingProfileSeed.STORAGE.category());
        assertEquals(BannerModSettlementBuildingCategory.MARKET, BannerModSettlementBuildingProfileSeed.MARKET.category());
        assertEquals(BannerModSettlementBuildingCategory.CONSTRUCTION, BannerModSettlementBuildingProfileSeed.CONSTRUCTION.category());
        assertEquals(BannerModSettlementBuildingCategory.GENERAL, BannerModSettlementBuildingProfileSeed.GENERAL.category());
    }

    @Test
    void fromBuildingTypeIdMapsKnownPathsAndFallsBackToGeneral() {
        assertEquals(BannerModSettlementBuildingProfileSeed.GENERAL,
                BannerModSettlementBuildingProfileSeed.fromBuildingTypeId(null));
        assertEquals(BannerModSettlementBuildingProfileSeed.GENERAL,
                BannerModSettlementBuildingProfileSeed.fromBuildingTypeId(" "));
        assertEquals(BannerModSettlementBuildingProfileSeed.FOOD_PRODUCTION,
                BannerModSettlementBuildingProfileSeed.fromBuildingTypeId("bannermod:crop_area"));
        assertEquals(BannerModSettlementBuildingProfileSeed.FOOD_PRODUCTION,
                BannerModSettlementBuildingProfileSeed.fromBuildingTypeId("animal_pen_area"));
        assertEquals(BannerModSettlementBuildingProfileSeed.MATERIAL_PRODUCTION,
                BannerModSettlementBuildingProfileSeed.fromBuildingTypeId("bannermod:mining_area"));
        assertEquals(BannerModSettlementBuildingProfileSeed.STORAGE,
                BannerModSettlementBuildingProfileSeed.fromBuildingTypeId("storage_area"));
        assertEquals(BannerModSettlementBuildingProfileSeed.MARKET,
                BannerModSettlementBuildingProfileSeed.fromBuildingTypeId("market_area"));
        assertEquals(BannerModSettlementBuildingProfileSeed.CONSTRUCTION,
                BannerModSettlementBuildingProfileSeed.fromBuildingTypeId("build_area"));
        assertEquals(BannerModSettlementBuildingProfileSeed.GENERAL,
                BannerModSettlementBuildingProfileSeed.fromBuildingTypeId("bannermod:watchtower"));
    }

    @Test
    void fromTagNameFallsBackToGeneralForBlankOrUnknownValues() {
        assertEquals(BannerModSettlementBuildingProfileSeed.GENERAL,
                BannerModSettlementBuildingProfileSeed.fromTagName(null));
        assertEquals(BannerModSettlementBuildingProfileSeed.GENERAL,
                BannerModSettlementBuildingProfileSeed.fromTagName(""));
        assertEquals(BannerModSettlementBuildingProfileSeed.GENERAL,
                BannerModSettlementBuildingProfileSeed.fromTagName("NOT_REAL"));
        assertEquals(BannerModSettlementBuildingProfileSeed.MARKET,
                BannerModSettlementBuildingProfileSeed.fromTagName("MARKET"));
    }
}
