package com.talhanation.bannermod.settlement.growth;

import com.talhanation.bannermod.settlement.BannerModSettlementBuildingCategory;
import com.talhanation.bannermod.settlement.BannerModSettlementBuildingProfileSeed;
import com.talhanation.bannermod.settlement.BannerModSettlementDesiredGoodSeed;
import com.talhanation.bannermod.settlement.BannerModSettlementDesiredGoodsSeed;
import com.talhanation.bannermod.settlement.BannerModSettlementMarketState;
import com.talhanation.bannermod.settlement.BannerModSettlementProjectCandidateSeed;
import com.talhanation.bannermod.settlement.BannerModSettlementStockpileSummary;
import com.talhanation.bannermod.settlement.BannerModSettlementSupplySignal;
import com.talhanation.bannermod.settlement.BannerModSettlementSupplySignalState;
import com.talhanation.bannermod.settlement.BannerModSettlementTradeRouteHandoffSeed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerModSettlementGrowthManagerTest {

    private static final BannerModSettlementMarketState NON_EMPTY_MARKET =
            new BannerModSettlementMarketState(1, 1, 0, 0, 0, 0, List.of(), List.of());

    @Test
    void emptySnapshotYieldsEmptyQueue() {
        List<PendingProject> queue = BannerModSettlementGrowthManager.evaluateGrowthQueue(emptyContext(), 8);
        assertTrue(queue.isEmpty(), "empty context should produce no candidates");
    }

    @Test
    void housingShortageYieldsNewBuildingInGeneralCategory() {
        // Residents exceed capacity and workers are unassigned → housing pressure.
        BannerModSettlementGrowthContext ctx = ctxOf(
                BannerModSettlementProjectCandidateSeed.empty(),
                BannerModSettlementDesiredGoodsSeed.empty(),
                BannerModSettlementMarketState.empty(),
                2, 2, 3,
                100L
        );

        List<PendingProject> queue = BannerModSettlementGrowthManager.evaluateGrowthQueue(ctx, 4);

        assertFalse(queue.isEmpty(), "saturated settlement should have a housing candidate");
        PendingProject top = queue.get(0);
        assertEquals(ProjectKind.NEW_BUILDING, top.kind());
        // Housing currently falls under GENERAL since no dedicated category exists.
        assertEquals(BannerModSettlementBuildingCategory.GENERAL, top.buildingCategory());
        assertSame(BannerModSettlementBuildingProfileSeed.GENERAL, top.profileSeed());
    }

    @Test
    void desiredGoodShortagePrioritisesMatchingProducer() {
        BannerModSettlementDesiredGoodsSeed desired = new BannerModSettlementDesiredGoodsSeed(List.of(
                new BannerModSettlementDesiredGoodSeed("food", 5)
        ));
        BannerModSettlementProjectCandidateSeed seed = new BannerModSettlementProjectCandidateSeed(
                "seed", BannerModSettlementBuildingProfileSeed.STORAGE, 0, false, false, List.of()
        );
        BannerModSettlementGrowthContext ctx = ctxOf(seed, desired, NON_EMPTY_MARKET, 0, 0, 0, 0L);

        List<PendingProject> queue = BannerModSettlementGrowthManager.evaluateGrowthQueue(ctx, 4);

        assertFalse(queue.isEmpty());
        PendingProject top = queue.get(0);
        assertSame(BannerModSettlementBuildingProfileSeed.FOOD_PRODUCTION, top.profileSeed());
        assertEquals(BannerModSettlementBuildingCategory.FOOD, top.buildingCategory());
    }

    @Test
    void reservationAwareHintsCanSeedDemandWithoutDesiredGoodsSeed() {
        BannerModSettlementTradeRouteHandoffSeed tradeRouteHandoffSeed = new BannerModSettlementTradeRouteHandoffSeed(
                1, 1, 0, 0, 2, 12,
                List.of(new BannerModSettlementDesiredGoodSeed("market_goods", 0)),
                List.of()
        );
        BannerModSettlementSupplySignalState supplySignalState = new BannerModSettlementSupplySignalState(
                1, 0, 0, 8,
                List.of(new BannerModSettlementSupplySignal("market_goods", 0, 0, 0, 8))
        );
        BannerModSettlementGrowthContext ctx = ctxOf(
                BannerModSettlementProjectCandidateSeed.empty(),
                BannerModSettlementDesiredGoodsSeed.empty(),
                NON_EMPTY_MARKET,
                tradeRouteHandoffSeed,
                supplySignalState,
                0, 0, 0, 0L
        );

        List<PendingProject> queue = BannerModSettlementGrowthManager.evaluateGrowthQueue(ctx, 4);

        assertFalse(queue.isEmpty());
        assertSame(BannerModSettlementBuildingProfileSeed.MARKET, queue.get(0).profileSeed());
    }

    @Test
    void pickNextProjectMirrorsTopOfQueue() {
        assertEquals(Optional.empty(), BannerModSettlementGrowthManager.pickNextProject(emptyContext()));

        BannerModSettlementDesiredGoodsSeed desired = new BannerModSettlementDesiredGoodsSeed(List.of(
                new BannerModSettlementDesiredGoodSeed("materials", 2)
        ));
        BannerModSettlementGrowthContext ctx = ctxOf(
                BannerModSettlementProjectCandidateSeed.empty(), desired, NON_EMPTY_MARKET, 0, 0, 0, 42L);

        List<PendingProject> queue = BannerModSettlementGrowthManager.evaluateGrowthQueue(ctx, 4);
        Optional<PendingProject> next = BannerModSettlementGrowthManager.pickNextProject(ctx);
        assertTrue(next.isPresent());
        assertEquals(queue.get(0), next.get());
    }

    @Test
    void maxQueueSizeZeroReturnsEmptyList() {
        BannerModSettlementDesiredGoodsSeed desired = new BannerModSettlementDesiredGoodsSeed(List.of(
                new BannerModSettlementDesiredGoodSeed("food", 3),
                new BannerModSettlementDesiredGoodSeed("materials", 3)
        ));
        BannerModSettlementGrowthContext ctx = ctxOf(
                BannerModSettlementProjectCandidateSeed.empty(), desired, NON_EMPTY_MARKET, 0, 0, 0, 0L);

        assertTrue(BannerModSettlementGrowthManager.evaluateGrowthQueue(ctx, 0).isEmpty());
        assertTrue(BannerModSettlementGrowthManager.evaluateGrowthQueue(ctx, -1).isEmpty());
    }

    @Test
    void tieBreakIsDeterministicOnOrdinalThenHash() {
        // "food" and "materials" both have driverCount=1 => identical base score.
        // FOOD (ordinal 0) precedes MATERIAL (ordinal 1), so the food candidate wins.
        BannerModSettlementDesiredGoodsSeed desired = new BannerModSettlementDesiredGoodsSeed(List.of(
                new BannerModSettlementDesiredGoodSeed("food", 1),
                new BannerModSettlementDesiredGoodSeed("materials", 1)
        ));
        BannerModSettlementGrowthContext ctx = ctxOf(
                BannerModSettlementProjectCandidateSeed.empty(), desired, NON_EMPTY_MARKET, 0, 0, 0, 7L);

        List<PendingProject> first = BannerModSettlementGrowthManager.evaluateGrowthQueue(ctx, 4);
        List<PendingProject> second = BannerModSettlementGrowthManager.evaluateGrowthQueue(ctx, 4);
        assertEquals(first, second, "deterministic ordering expected across invocations");
        assertTrue(first.size() >= 2);
        assertEquals(first.get(0).priorityScore(), first.get(1).priorityScore(),
                "first two candidates must be a genuine tie on score for this test");
        assertSame(BannerModSettlementBuildingProfileSeed.FOOD_PRODUCTION, first.get(0).profileSeed());
        assertSame(BannerModSettlementBuildingProfileSeed.MATERIAL_PRODUCTION, first.get(1).profileSeed());
        assertNotEquals(first.get(0), first.get(1));
    }

    private static BannerModSettlementGrowthContext emptyContext() {
        return ctxOf(
                BannerModSettlementProjectCandidateSeed.empty(),
                BannerModSettlementDesiredGoodsSeed.empty(),
                BannerModSettlementMarketState.empty(),
                0, 0, 0, 0L
        );
    }

    private static BannerModSettlementGrowthContext ctxOf(
            BannerModSettlementProjectCandidateSeed seed,
            BannerModSettlementDesiredGoodsSeed desired,
            BannerModSettlementMarketState market,
            int residentCapacity,
            int assignedResidentCount,
            int unassignedWorkerCount,
            long gameTime
    ) {
        return new BannerModSettlementGrowthContext(
                seed, desired,
                BannerModSettlementStockpileSummary.empty(),
                market,
                BannerModSettlementTradeRouteHandoffSeed.empty(),
                BannerModSettlementSupplySignalState.empty(),
                List.of(), List.of(),
                residentCapacity, assignedResidentCount, unassignedWorkerCount, 0,
                null, gameTime
        );
    }

    private static BannerModSettlementGrowthContext ctxOf(
            BannerModSettlementProjectCandidateSeed seed,
            BannerModSettlementDesiredGoodsSeed desired,
            BannerModSettlementMarketState market,
            BannerModSettlementTradeRouteHandoffSeed tradeRouteHandoffSeed,
            BannerModSettlementSupplySignalState supplySignalState,
            int residentCapacity,
            int assignedResidentCount,
            int unassignedWorkerCount,
            long gameTime
    ) {
        return new BannerModSettlementGrowthContext(
                seed,
                desired,
                BannerModSettlementStockpileSummary.empty(),
                market,
                tradeRouteHandoffSeed,
                supplySignalState,
                List.of(),
                List.of(),
                residentCapacity,
                assignedResidentCount,
                unassignedWorkerCount,
                0,
                null,
                gameTime
        );
    }
}
