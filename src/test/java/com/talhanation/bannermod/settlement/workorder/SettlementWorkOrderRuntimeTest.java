package com.talhanation.bannermod.settlement.workorder;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

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
    void completeRecordsExecutionReceipt() {
        SettlementWorkOrderRuntime runtime = new SettlementWorkOrderRuntime();
        BlockPos target = new BlockPos(1, 64, 1);
        runtime.publish(SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.BUILD_BLOCK, target, null, 70, 10L));
        SettlementWorkOrder claimed = runtime.claim(CLAIM_A, RESIDENT_A, null, 100L, 0L).orElseThrow();

        runtime.complete(claimed.orderUuid(), 1234L);

        assertEquals(0, runtime.size());
        List<SettlementWorkOrderExecutionReceipt> receipts = runtime.recentCompletions();
        assertEquals(1, receipts.size());
        SettlementWorkOrderExecutionReceipt receipt = receipts.get(0);
        assertEquals(claimed.orderUuid(), receipt.orderUuid());
        assertEquals(CLAIM_A, receipt.claimUuid());
        assertEquals(BUILDING_A, receipt.buildingUuid());
        assertEquals(SettlementWorkOrderType.BUILD_BLOCK, receipt.type());
        assertEquals(target, receipt.targetPos());
        assertEquals(RESIDENT_A, receipt.claimedByResidentUuid());
        assertEquals(1234L, receipt.completedGameTime());
    }

    @Test
    void recentCompletionReceiptsAreBounded() {
        SettlementWorkOrderRuntime runtime = new SettlementWorkOrderRuntime();
        int total = SettlementWorkOrderRuntime.MAX_RECENT_COMPLETION_RECEIPTS + 4;
        for (int i = 0; i < total; i++) {
            SettlementWorkOrder order = SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                    SettlementWorkOrderType.HARVEST_CROP, new BlockPos(i, 64, 1), null, 70, i);
            runtime.publish(order);
            runtime.complete(order.orderUuid(), i);
        }

        List<SettlementWorkOrderExecutionReceipt> receipts = runtime.recentCompletions();
        assertEquals(SettlementWorkOrderRuntime.MAX_RECENT_COMPLETION_RECEIPTS, receipts.size());
        assertEquals(4L, receipts.get(0).completedGameTime());
        assertEquals((long) total - 1L, receipts.get(receipts.size() - 1).completedGameTime());
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

    @Test
    void restoreSnapshotRebuildsClaimIndexesAndResidentClaims() {
        SettlementWorkOrderRuntime source = new SettlementWorkOrderRuntime();
        source.publish(SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.HARVEST_CROP, new BlockPos(1, 64, 1), null, 70, 10L));
        source.publish(SettlementWorkOrder.pending(CLAIM_A, BUILDING_B,
                SettlementWorkOrderType.MINE_BLOCK, new BlockPos(2, 64, 2), null, 60, 12L));
        SettlementWorkOrder claimed = source.claim(CLAIM_A, RESIDENT_A, null, 100L, 20L).orElseThrow();

        SettlementWorkOrderRuntime restored = new SettlementWorkOrderRuntime();
        restored.restoreSnapshot(source.snapshot());

        assertEquals(2, restored.size());
        assertTrue(restored.recentCompletions().isEmpty());
        assertEquals(claimed.orderUuid(), restored.currentClaim(RESIDENT_A).orElseThrow().orderUuid());
        assertEquals(1, restored.pendingFor(CLAIM_A).size());
        assertEquals(1, restored.countForClaim(CLAIM_A, SettlementWorkOrderStatus.CLAIMED));
        assertEquals(1, restored.ordersForBuilding(BUILDING_A).size());
        assertEquals(1, restored.ordersForBuilding(BUILDING_B).size());
    }

    @Test
    void restoreSnapshotMarksDirtyOnlyWhenPersistedOrdersChange() {
        SettlementWorkOrderRuntime runtime = new SettlementWorkOrderRuntime();
        AtomicInteger dirtyCount = new AtomicInteger();
        runtime.setDirtyListener(dirtyCount::incrementAndGet);

        runtime.restoreSnapshot(List.of());
        assertEquals(0, dirtyCount.get());

        SettlementWorkOrder order = SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.HARVEST_CROP, new BlockPos(1, 64, 1), null, 70, 10L);

        runtime.restoreSnapshot(List.of(order));
        assertEquals(1, dirtyCount.get());

        runtime.restoreSnapshot(List.of(order));
        assertEquals(1, dirtyCount.get());

        runtime.restoreSnapshot(List.of());
        assertEquals(2, dirtyCount.get());
    }

    @Test
    void workOrderTagRoundTripPreservesFields() {
        SettlementWorkOrder source = new SettlementWorkOrder(
                UUID.fromString("00000000-0000-0000-0000-0000000000d1"),
                CLAIM_A,
                BUILDING_A,
                SettlementWorkOrderType.HAUL_RESOURCE,
                new BlockPos(3, 65, 7),
                "minecraft:wheat",
                91,
                1234L,
                SettlementWorkOrderStatus.CLAIMED,
                RESIDENT_A,
                2345L
        );

        SettlementWorkOrder restored = SettlementWorkOrder.fromTag(source.toTag());

        assertEquals(source, restored);
    }

    @Test
    void transportOrderClaimReleaseAndCompleteCycleHonoursPayload() {
        SettlementWorkOrderRuntime runtime = new SettlementWorkOrderRuntime();
        SettlementWorkOrder published = runtime.publish(SettlementWorkOrder.pendingTransport(
                CLAIM_A,
                BUILDING_A,
                SettlementWorkOrderType.HAUL_RESOURCE,
                new BlockPos(10, 64, 10),
                new BlockPos(20, 64, 20),
                "minecraft:wheat",
                32,
                75,
                100L
        )).orElseThrow();

        SettlementWorkOrder claimed = runtime.claim(CLAIM_A, RESIDENT_A, null, 200L, 50L).orElseThrow();
        assertEquals(published.orderUuid(), claimed.orderUuid());
        assertEquals(SettlementWorkOrderType.HAUL_RESOURCE, claimed.type());
        assertEquals(new BlockPos(10, 64, 10), claimed.sourcePos());
        assertEquals(new BlockPos(20, 64, 20), claimed.destinationPos());
        assertEquals(new BlockPos(20, 64, 20), claimed.targetPos());
        assertEquals("minecraft:wheat", claimed.resourceHintId());
        assertEquals(32, claimed.itemCount());

        runtime.release(claimed.orderUuid());
        assertTrue(runtime.currentClaim(RESIDENT_A).isEmpty());
        SettlementWorkOrder pending = runtime.pendingFor(CLAIM_A).get(0);
        assertEquals(claimed.orderUuid(), pending.orderUuid());
        assertEquals(SettlementWorkOrderStatus.PENDING, pending.status());
        assertEquals(claimed.sourcePos(), pending.sourcePos());
        assertEquals(claimed.destinationPos(), pending.destinationPos());
        assertEquals(claimed.itemCount(), pending.itemCount());

        SettlementWorkOrder reclaimed = runtime.claim(CLAIM_A, RESIDENT_A, null, 300L, 50L).orElseThrow();
        runtime.complete(reclaimed.orderUuid(), 400L);
        assertEquals(0, runtime.size());
        assertEquals(1, runtime.recentCompletions().size());
        SettlementWorkOrderExecutionReceipt receipt = runtime.recentCompletions().get(0);
        assertEquals(SettlementWorkOrderType.HAUL_RESOURCE, receipt.type());
        assertEquals(new BlockPos(20, 64, 20), receipt.targetPos());
    }

    @Test
    void transportOrderRepublishIsRejectedAsDuplicateForSameDestination() {
        SettlementWorkOrderRuntime runtime = new SettlementWorkOrderRuntime();
        SettlementWorkOrder first = SettlementWorkOrder.pendingTransport(
                CLAIM_A,
                BUILDING_A,
                SettlementWorkOrderType.HAUL_RESOURCE,
                new BlockPos(0, 64, 0),
                new BlockPos(5, 64, 5),
                null,
                16,
                60,
                100L
        );
        SettlementWorkOrder secondAttempt = SettlementWorkOrder.pendingTransport(
                CLAIM_A,
                BUILDING_A,
                SettlementWorkOrderType.HAUL_RESOURCE,
                new BlockPos(0, 64, 0),
                new BlockPos(5, 64, 5),
                "minecraft:wheat",
                64,
                80,
                200L
        );

        assertTrue(runtime.publish(first).isPresent());
        assertTrue(runtime.publish(secondAttempt).isEmpty());
        assertEquals(1, runtime.size());
    }

    @Test
    void transportWorkOrderPayloadRoundTripPreservesSourceDestinationAndCount() {
        SettlementWorkOrder source = SettlementWorkOrder.pendingTransport(
                CLAIM_A,
                BUILDING_A,
                SettlementWorkOrderType.FETCH_INPUT,
                new BlockPos(1, 64, 1),
                new BlockPos(3, 64, 3),
                "minecraft:wheat",
                16,
                90,
                123L
        );

        SettlementWorkOrder restored = SettlementWorkOrder.fromTag(source.toTag());

        assertEquals(source, restored);
        assertEquals(new BlockPos(1, 64, 1), restored.sourcePos());
        assertEquals(new BlockPos(3, 64, 3), restored.destinationPos());
        assertEquals(16, restored.itemCount());
    }

    @Test
    void workOrderTagRoundTripPreservesNullOptionals() {
        SettlementWorkOrder source = SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.STOCK_MARKET, null, null, 12, 44L);

        SettlementWorkOrder restored = SettlementWorkOrder.fromTag(source.toTag());

        assertEquals(source, restored);
    }

    @Test
    void runtimeTagRoundTripUsesSnapshotRestoreSemantics() {
        SettlementWorkOrderRuntime source = new SettlementWorkOrderRuntime();
        source.publish(SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.HARVEST_CROP, new BlockPos(1, 64, 1), null, 70, 10L));
        source.publish(SettlementWorkOrder.pending(CLAIM_A, BUILDING_B,
                SettlementWorkOrderType.MINE_BLOCK, new BlockPos(2, 64, 2), "minecraft:stone", 60, 12L));
        SettlementWorkOrder claimed = source.claim(CLAIM_A, RESIDENT_A, null, 100L, 20L).orElseThrow();

        CompoundTag tag = source.toTag();
        SettlementWorkOrderRuntime restored = SettlementWorkOrderRuntime.fromTag(tag);

        assertEquals(source.snapshot(), restored.snapshot());
        assertEquals(claimed.orderUuid(), restored.currentClaim(RESIDENT_A).orElseThrow().orderUuid());
        assertEquals(1, restored.pendingFor(CLAIM_A).size());
        assertEquals(1, restored.countForClaim(CLAIM_A, SettlementWorkOrderStatus.CLAIMED));
        assertEquals(1, restored.ordersForBuilding(BUILDING_A).size());
        assertEquals(1, restored.ordersForBuilding(BUILDING_B).size());
    }

    @Test
    void savedDataRoundTripRestoresRuntimeSnapshot() {
        SettlementWorkOrderSavedData source = new SettlementWorkOrderSavedData();
        source.runtime().publish(SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.HARVEST_CROP, new BlockPos(1, 64, 1), null, 70, 10L));
        source.runtime().publish(SettlementWorkOrder.pending(CLAIM_A, BUILDING_B,
                SettlementWorkOrderType.MINE_BLOCK, new BlockPos(2, 64, 2), "minecraft:stone", 60, 12L));
        SettlementWorkOrder claimed = source.runtime().claim(CLAIM_A, RESIDENT_A, null, 100L, 20L).orElseThrow();

        SettlementWorkOrderSavedData restored = SettlementWorkOrderSavedData.load(source.save(new CompoundTag()));

        assertEquals(source.runtime().snapshot(), restored.runtime().snapshot());
        assertEquals(claimed.orderUuid(), restored.runtime().currentClaim(RESIDENT_A).orElseThrow().orderUuid());
        assertEquals(1, restored.runtime().pendingFor(CLAIM_A).size());
        assertEquals(1, restored.runtime().countForClaim(CLAIM_A, SettlementWorkOrderStatus.CLAIMED));
    }

    @Test
    void dirtyListenerRunsOnlyForEffectiveRuntimeMutations() {
        SettlementWorkOrderRuntime runtime = new SettlementWorkOrderRuntime();
        AtomicInteger dirtyCount = new AtomicInteger();
        runtime.setDirtyListener(dirtyCount::incrementAndGet);
        SettlementWorkOrder first = SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.HARVEST_CROP, new BlockPos(1, 64, 1), null, 70, 10L);
        SettlementWorkOrder duplicate = SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.HARVEST_CROP, new BlockPos(1, 64, 1), null, 80, 12L);

        runtime.publish(first);
        runtime.publish(duplicate);
        runtime.release(UUID.randomUUID());
        SettlementWorkOrder claimed = runtime.claim(CLAIM_A, RESIDENT_A, null, 100L, 20L).orElseThrow();
        runtime.complete(claimed.orderUuid());

        assertEquals(3, dirtyCount.get());
    }

    @Test
    void purgeMarksDirtyOnlyWhenOrdersAreRemoved() {
        SettlementWorkOrderRuntime runtime = new SettlementWorkOrderRuntime();
        AtomicInteger dirtyCount = new AtomicInteger();
        runtime.setDirtyListener(dirtyCount::incrementAndGet);
        runtime.publish(SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.HARVEST_CROP, new BlockPos(1, 64, 1), null, 70, 10L));

        assertEquals(0, runtime.purgeBuilding(BUILDING_B));
        assertEquals(1, runtime.purgeClaim(CLAIM_A));

        assertEquals(2, dirtyCount.get());
    }

    @Test
    void snapshotIsDefensiveCopy() {
        SettlementWorkOrderRuntime runtime = new SettlementWorkOrderRuntime();
        runtime.publish(SettlementWorkOrder.pending(CLAIM_A, BUILDING_A,
                SettlementWorkOrderType.HARVEST_CROP, new BlockPos(1, 64, 1), null, 70, 10L));

        List<SettlementWorkOrder> snapshot = runtime.snapshot();
        runtime.purgeClaim(CLAIM_A);

        assertEquals(1, snapshot.size());
        assertEquals(0, runtime.size());
    }
}
