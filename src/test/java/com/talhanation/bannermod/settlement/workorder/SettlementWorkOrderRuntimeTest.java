package com.talhanation.bannermod.settlement.workorder;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettlementWorkOrderRuntimeTest {

    private static final UUID CLAIM_A = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final UUID BUILDING_A = UUID.fromString("00000000-0000-0000-0000-0000000000b1");
    private static final UUID BUILDING_B = UUID.fromString("00000000-0000-0000-0000-0000000000b2");
    private static final UUID RESIDENT_A = UUID.fromString("00000000-0000-0000-0000-0000000000c1");
    private static final UUID RESIDENT_B = UUID.fromString("00000000-0000-0000-0000-0000000000c2");

    @Test
    void publishRejectsDuplicatePositionAndType() {
        SettlementWorkOrderRuntime runtime = new SettlementWorkOrderRuntime();
        SettlementWorkOrder first = SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.HARVEST_CROP, new BlockPos(1, 64, 1), null, 50, 100L);
        SettlementWorkOrder duplicate = SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.HARVEST_CROP, new BlockPos(1, 64, 1), null, 80, 200L);

        assertTrue(runtime.publish(first).isPresent());
        assertTrue(runtime.publish(duplicate).isEmpty());
        assertEquals(1, runtime.size());
    }

    @Test
    void claimReturnsHighestPriorityFirst() {
        SettlementWorkOrderRuntime runtime = new SettlementWorkOrderRuntime();
        runtime.publish(SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.PLANT_CROP, new BlockPos(1, 64, 1), null, 40, 10L));
        runtime.publish(SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.HARVEST_CROP, new BlockPos(1, 64, 2), null, 80, 12L));
        runtime.publish(SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.TILL_SOIL, new BlockPos(1, 64, 3), null, 60, 14L));

        Optional<SettlementWorkOrder> picked = runtime.claim(CLAIM_A, RESIDENT_A, null, 100L, 200L);

        assertTrue(picked.isPresent());
        assertEquals(SettlementWorkOrderType.HARVEST_CROP, picked.get().type());
        assertEquals(SettlementWorkOrderStatus.CLAIMED, picked.get().status());
        assertEquals(RESIDENT_A, picked.get().claimedByResidentUuid());
        assertEquals(300L, picked.get().claimExpiryGameTime());
    }

    @Test
    void claimRespectsFilter() {
        SettlementWorkOrderRuntime runtime = new SettlementWorkOrderRuntime();
        runtime.publish(SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.BUILD_BLOCK, new BlockPos(1, 64, 1), null, 90, 10L));
        runtime.publish(SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.HARVEST_CROP, new BlockPos(1, 64, 2), null, 50, 12L));

        Optional<SettlementWorkOrder> picked = runtime.claim(
                CLAIM_A,
                RESIDENT_A,
                o -> o.type() == SettlementWorkOrderType.HARVEST_CROP,
                100L,
                0L
        );
        assertTrue(picked.isPresent());
        assertEquals(SettlementWorkOrderType.HARVEST_CROP, picked.get().type());
    }

    @Test
    void claimReturnsExistingClaimForResident() {
        SettlementWorkOrderRuntime runtime = new SettlementWorkOrderRuntime();
        runtime.publish(SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.HARVEST_CROP, new BlockPos(1, 64, 1), null, 70, 10L));
        runtime.publish(SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.HARVEST_CROP, new BlockPos(1, 64, 2), null, 70, 12L));

        SettlementWorkOrder first = runtime.claim(CLAIM_A, RESIDENT_A, null, 100L, 0L).orElseThrow();
        SettlementWorkOrder second = runtime.claim(CLAIM_A, RESIDENT_A, null, 101L, 0L).orElseThrow();
        assertEquals(first.orderUuid(), second.orderUuid());
    }

    @Test
    void claimForBuildingScopesToOneBuilding() {
        SettlementWorkOrderRuntime runtime = new SettlementWorkOrderRuntime();
        runtime.publish(SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.HARVEST_CROP, new BlockPos(1, 64, 1), null, 50, 10L));
        runtime.publish(SettlementWorkOrder.pending(CLAIM_A, BUILDING_B,
                SettlementWorkOrderType.HARVEST_CROP, new BlockPos(2, 64, 2), null, 99, 12L));

        Optional<SettlementWorkOrder> picked = runtime.claimForBuilding(BUILDING_A, RESIDENT_A, null, 100L, 0L);

        assertTrue(picked.isPresent());
        assertEquals(BUILDING_A, picked.get().buildingUuid());
    }

    @Test
    void releaseReturnsOrderToPending() {
        SettlementWorkOrderRuntime runtime = new SettlementWorkOrderRuntime();
        runtime.publish(SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.HARVEST_CROP, new BlockPos(1, 64, 1), null, 70, 10L));

        SettlementWorkOrder claimed = runtime.claim(CLAIM_A, RESIDENT_A, null, 100L, 0L).orElseThrow();
        runtime.release(claimed.orderUuid());

        assertTrue(runtime.currentClaim(RESIDENT_A).isEmpty());
        assertEquals(1, runtime.pendingFor(CLAIM_A).size());
    }

    @Test
    void completeRemovesOrder() {
        SettlementWorkOrderRuntime runtime = new SettlementWorkOrderRuntime();
        runtime.publish(SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.HARVEST_CROP, new BlockPos(1, 64, 1), null, 70, 10L));
        SettlementWorkOrder claimed = runtime.claim(CLAIM_A, RESIDENT_A, null, 100L, 0L).orElseThrow();

        runtime.complete(claimed.orderUuid());

        assertEquals(0, runtime.size());
        assertTrue(runtime.currentClaim(RESIDENT_A).isEmpty());
    }

    @Test
    void reclaimAbandonedReleasesExpiredClaims() {
        SettlementWorkOrderRuntime runtime = new SettlementWorkOrderRuntime();
        runtime.publish(SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.HARVEST_CROP, new BlockPos(1, 64, 1), null, 70, 10L));
        SettlementWorkOrder claimed = runtime.claim(CLAIM_A, RESIDENT_A, null, 100L, 10L).orElseThrow();
        assertEquals(110L, claimed.claimExpiryGameTime());

        int reclaimed = runtime.reclaimAbandoned(200L);

        assertEquals(1, reclaimed);
        assertTrue(runtime.currentClaim(RESIDENT_A).isEmpty());
        assertEquals(1, runtime.pendingFor(CLAIM_A).size());
    }

    @Test
    void purgeClaimRemovesAllOrdersForClaim() {
        SettlementWorkOrderRuntime runtime = new SettlementWorkOrderRuntime();
        runtime.publish(SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.HARVEST_CROP, new BlockPos(1, 64, 1), null, 70, 10L));
        runtime.publish(SettlementWorkOrder.pending(CLAIM_A, BUILDING_B,
                SettlementWorkOrderType.MINE_BLOCK, new BlockPos(2, 64, 2), null, 70, 12L));

        int purged = runtime.purgeClaim(CLAIM_A);

        assertEquals(2, purged);
        assertEquals(0, runtime.size());
    }

    @Test
    void differentResidentsClaimDifferentOrders() {
        SettlementWorkOrderRuntime runtime = new SettlementWorkOrderRuntime();
        runtime.publish(SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.HARVEST_CROP, new BlockPos(1, 64, 1), null, 70, 10L));
        runtime.publish(SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.HARVEST_CROP, new BlockPos(1, 64, 2), null, 70, 12L));

        SettlementWorkOrder first = runtime.claim(CLAIM_A, RESIDENT_A, null, 100L, 0L).orElseThrow();
        SettlementWorkOrder second = runtime.claim(CLAIM_A, RESIDENT_B, null, 100L, 0L).orElseThrow();

        assertFalse(first.orderUuid().equals(second.orderUuid()));
        assertEquals(RESIDENT_A, first.claimedByResidentUuid());
        assertEquals(RESIDENT_B, second.claimedByResidentUuid());
    }
}
