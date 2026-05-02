package com.talhanation.bannermod.settlement.growth;

import com.talhanation.bannermod.governance.BannerModGovernorSnapshot;
import com.talhanation.bannermod.settlement.BannerModSettlementDesiredGoodSeed;
import com.talhanation.bannermod.settlement.BannerModSettlementDesiredGoodsSeed;
import com.talhanation.bannermod.settlement.BannerModSettlementMarketState;
import com.talhanation.bannermod.settlement.BannerModSettlementProjectCandidateSeed;
import com.talhanation.bannermod.settlement.BannerModSettlementSnapshot;
import com.talhanation.bannermod.settlement.BannerModSettlementStockpileSummary;
import com.talhanation.bannermod.settlement.BannerModSettlementSupplySignalState;
import com.talhanation.bannermod.settlement.BannerModSettlementTradeRouteHandoffSeed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerModSettlementGrowthContextTest {

    @Test
    void constructorNormalizesNullSeedsAndNegativeCounts() {
        BannerModSettlementGrowthContext ctx = new BannerModSettlementGrowthContext(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                -2,
                -3,
                -4,
                -5,
                null,
                7L
        );

        assertEquals(BannerModSettlementProjectCandidateSeed.empty(), ctx.projectCandidateSeed());
        assertEquals(BannerModSettlementDesiredGoodsSeed.empty(), ctx.desiredGoodsSeed());
        assertEquals(BannerModSettlementStockpileSummary.empty(), ctx.stockpileSummary());
        assertEquals(BannerModSettlementMarketState.empty(), ctx.marketState());
        assertEquals(BannerModSettlementTradeRouteHandoffSeed.empty(), ctx.tradeRouteHandoffSeed());
        assertEquals(BannerModSettlementSupplySignalState.empty(), ctx.supplySignalState());
        assertTrue(ctx.buildings().isEmpty());
        assertTrue(ctx.residents().isEmpty());
        assertEquals(0, ctx.residentCapacity());
        assertEquals(0, ctx.assignedResidentCount());
        assertEquals(0, ctx.unassignedWorkerCount());
        assertEquals(0, ctx.missingWorkAreaAssignmentCount());
        assertEquals(0, ctx.housingHeadroom());
        assertFalse(ctx.isUnderSiege());
    }

    @Test
    void fromSnapshotCopiesSnapshotFieldsAndCalculatesHeadroom() {
        BannerModSettlementSnapshot snapshot = snapshot(3, 1, 2, 1);

        BannerModSettlementGrowthContext ctx = BannerModSettlementGrowthContext.fromSnapshot(snapshot, 55L);

        assertEquals(snapshot.projectCandidateSeed(), ctx.projectCandidateSeed());
        assertEquals(snapshot.desiredGoodsSeed(), ctx.desiredGoodsSeed());
        assertEquals(snapshot.stockpileSummary(), ctx.stockpileSummary());
        assertEquals(snapshot.marketState(), ctx.marketState());
        assertEquals(snapshot.tradeRouteHandoffSeed(), ctx.tradeRouteHandoffSeed());
        assertEquals(snapshot.supplySignalState(), ctx.supplySignalState());
        assertEquals(2, ctx.housingHeadroom());
        assertEquals(55L, ctx.gameTime());
        assertEquals(null, ctx.governorSnapshot());
    }

    @Test
    void fromSnapshotDetectsUnderSiegeCaseInsensitivelyAndRejectsNullSnapshot() {
        BannerModGovernorSnapshot governorSnapshot = new BannerModGovernorSnapshot(
                UUID.randomUUID(),
                0,
                0,
                null,
                null,
                null,
                0L,
                0L,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                List.of("Under_Siege", "tax_warning"),
                List.of()
        );

        BannerModSettlementGrowthContext ctx = BannerModSettlementGrowthContext.fromSnapshot(snapshot(0, 0, 0, 0), governorSnapshot, 10L);

        assertTrue(ctx.isUnderSiege());
        assertEquals(governorSnapshot, ctx.governorSnapshot());
        assertThrows(IllegalArgumentException.class, () -> BannerModSettlementGrowthContext.fromSnapshot(null, 10L));
    }

    private static BannerModSettlementSnapshot snapshot(int residentCapacity,
                                                        int assignedResidentCount,
                                                        int unassignedWorkerCount,
                                                        int missingWorkAreaAssignmentCount) {
        return new BannerModSettlementSnapshot(
                UUID.randomUUID(),
                0,
                0,
                "blueguild",
                10L,
                residentCapacity,
                assignedResidentCount,
                unassignedWorkerCount,
                missingWorkAreaAssignmentCount,
                0,
                0,
                BannerModSettlementStockpileSummary.empty(),
                BannerModSettlementMarketState.empty(),
                new BannerModSettlementDesiredGoodsSeed(List.of(new BannerModSettlementDesiredGoodSeed("food", 2))),
                new BannerModSettlementProjectCandidateSeed(
                        "seed",
                        com.talhanation.bannermod.settlement.BannerModSettlementBuildingProfileSeed.GENERAL,
                        2,
                        true,
                        true,
                        List.of("housing_pressure")
                ),
                BannerModSettlementTradeRouteHandoffSeed.empty(),
                BannerModSettlementSupplySignalState.empty(),
                List.of(),
                List.of()
        );
    }
}
