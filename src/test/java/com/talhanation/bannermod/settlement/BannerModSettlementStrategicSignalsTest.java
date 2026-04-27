package com.talhanation.bannermod.settlement;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerModSettlementStrategicSignalsTest {
    @Test
    void classifiesFoodAndStorageAsSurplusHubWithWarObjective() {
        BannerModSettlementStrategicSignals signals = BannerModSettlementStrategicSignals.fromSnapshot(snapshot(
                new BannerModSettlementStockpileSummary(1, 2, 54, 0, 0, List.of()),
                BannerModSettlementMarketState.empty(),
                List.of(
                        building("bannermod:crop_area", BannerModSettlementBuildingProfileSeed.FOOD_PRODUCTION),
                        building("bannermod:storage_area", BannerModSettlementBuildingProfileSeed.STORAGE)
                )
        ));

        assertEquals("surplus_hub", signals.roleId());
        assertEquals("landlocked", signals.routeCostId());
        assertEquals("preserved_food", signals.specializationId());
        assertTrue(signals.logisticsObjectiveIds().contains("surplus_store"));
        assertTrue(signals.loyaltyPressureIds().contains("isolated_supply"));
    }

    @Test
    void waterAccessBecomesWaterGateAndCheapRoute() {
        BannerModSettlementStrategicSignals signals = BannerModSettlementStrategicSignals.fromSnapshot(snapshot(
                new BannerModSettlementStockpileSummary(1, 2, 54, 1, 1, List.of()),
                new BannerModSettlementMarketState(1, 1, 27, 20, 0, 0, List.of(), List.of()),
                List.of(building("bannermod:storage_area", BannerModSettlementBuildingProfileSeed.STORAGE))
        ));

        assertEquals("water_gate", signals.roleId());
        assertEquals("water_advantaged", signals.routeCostId());
        assertTrue(signals.logisticsObjectiveIds().contains("water_gate"));
        assertTrue(signals.loyaltyPressureIds().isEmpty());
    }

    private static BannerModSettlementSnapshot snapshot(BannerModSettlementStockpileSummary stockpileSummary,
                                                        BannerModSettlementMarketState marketState,
                                                        List<BannerModSettlementBuildingRecord> buildings) {
        ChunkPos anchor = new ChunkPos(0, 0);
        return new BannerModSettlementSnapshot(
                UUID.randomUUID(),
                anchor.x,
                anchor.z,
                null,
                0L,
                0,
                0,
                0,
                0,
                0,
                0,
                stockpileSummary,
                marketState,
                BannerModSettlementDesiredGoodsSeed.empty(),
                BannerModSettlementProjectCandidateSeed.empty(),
                BannerModSettlementTradeRouteHandoffSeed.empty(),
                BannerModSettlementSupplySignalState.empty(),
                List.of(),
                buildings
        );
    }

    private static BannerModSettlementBuildingRecord building(String typeId, BannerModSettlementBuildingProfileSeed profileSeed) {
        return new BannerModSettlementBuildingRecord(
                UUID.randomUUID(),
                typeId,
                BlockPos.ZERO,
                null,
                null,
                0,
                0,
                0,
                List.of(),
                false,
                0,
                0,
                false,
                false,
                List.of(),
                profileSeed.category(),
                profileSeed
        );
    }
}
