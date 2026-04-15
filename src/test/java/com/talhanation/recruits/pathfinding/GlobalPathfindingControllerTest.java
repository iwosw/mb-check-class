package com.talhanation.recruits.pathfinding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalPathfindingControllerTest {

    @BeforeEach
    void setUp() {
        GlobalPathfindingController.resetProfiling();
        GlobalPathfindingController.configureBudgetForTests(16, 8, 20);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        GlobalPathfindingController.clearBudgetOverrideForTests();
    }

    @Test
    void compatibleSecondRequestReusesPreviousPathWithoutCallingSupplierAgain() {
        AtomicInteger supplierCalls = new AtomicInteger();
        Path expected = createPath(new BlockPos(4, 0, 4), new BlockPos(5, 0, 5));
        GlobalPathfindingController.PathRequest firstRequest = blockRequest(new BlockPos(0, 0, 0), new BlockPos(5, 0, 5), 20);
        GlobalPathfindingController.PathRequest secondRequest = blockRequest(new BlockPos(1, 0, 0), new BlockPos(6, 0, 5), 22);

        GlobalPathfindingController.PathRequestResult<Path> initialResult = GlobalPathfindingController.requestPath(firstRequest, null, () -> {
            supplierCalls.incrementAndGet();
            return expected;
        });

        GlobalPathfindingController.PathRequestResult<Path> reusedResult = GlobalPathfindingController.requestPath(secondRequest, null, () -> {
            supplierCalls.incrementAndGet();
            return createPath(new BlockPos(9, 0, 9), new BlockPos(10, 0, 10));
        });

        GlobalPathfindingController.ProfilingSnapshot snapshot = GlobalPathfindingController.profilingSnapshot();

        assertEquals(GlobalPathfindingController.RequestStatus.EXECUTED, initialResult.status());
        assertEquals(GlobalPathfindingController.RequestStatus.EXECUTED, reusedResult.status());
        assertSame(expected, initialResult.result());
        assertNotNull(reusedResult.result());
        assertNotSame(expected, reusedResult.result());
        assertEquals(expected.getNodeCount(), reusedResult.result().getNodeCount());
        assertEquals(expected.getTarget(), reusedResult.result().getTarget());
        assertEquals(1, supplierCalls.get());
        assertEquals(2, snapshot.totalRequests());
        assertEquals(2, snapshot.blockTargetRequests());
        assertEquals(0, snapshot.entityTargetRequests());
        assertEquals(2, snapshot.asyncEnabledRequests());
        assertEquals(0, snapshot.asyncDisabledRequests());
        assertEquals(2, snapshot.targetPositionsObserved());
        assertEquals(2, snapshot.reuseAttempts());
        assertEquals(1, snapshot.reuseHits());
        assertEquals(1, snapshot.reuseMisses());
        assertEquals(1, snapshot.reuseMissesNoCandidate());
    }

    @Test
    void staleNullDoneAndIncompatibleCandidatesAreRejectedAndCountedAsDrops() {
        GlobalPathfindingController.PathRequest request = blockRequest(new BlockPos(0, 0, 0), new BlockPos(4, 0, 4), 20);
        GlobalPathfindingController.PathRequest staleRequest = blockRequest(new BlockPos(0, 0, 0), new BlockPos(4, 0, 4), 60);
        GlobalPathfindingController.PathRequest incompatibleRequest = entityRequest(new BlockPos(0, 0, 0), new BlockPos(4, 0, 4), 20);

        GlobalPathfindingController.rememberCandidateForTests(request, null);
        assertNull(GlobalPathfindingController.tryReuseForTests(request));

        Path donePath = createPath(new BlockPos(4, 0, 4), new BlockPos(5, 0, 5));
        donePath.advance();
        donePath.advance();
        GlobalPathfindingController.rememberCandidateForTests(request, donePath);
        assertNull(GlobalPathfindingController.tryReuseForTests(request));

        GlobalPathfindingController.rememberCandidateForTests(request, createPath(new BlockPos(4, 0, 4), new BlockPos(5, 0, 5)));
        assertNull(GlobalPathfindingController.tryReuseForTests(incompatibleRequest));

        GlobalPathfindingController.rememberCandidateForTests(request, createPath(new BlockPos(4, 0, 4), new BlockPos(5, 0, 5)));
        assertNull(GlobalPathfindingController.tryReuseForTests(staleRequest));

        GlobalPathfindingController.ProfilingSnapshot snapshot = GlobalPathfindingController.profilingSnapshot();

        assertEquals(4, snapshot.reuseAttempts());
        assertEquals(0, snapshot.reuseHits());
        assertEquals(4, snapshot.reuseMisses());
        assertEquals(1, snapshot.reuseDropsNullCandidate());
        assertEquals(1, snapshot.reuseDropsDoneCandidate());
        assertEquals(1, snapshot.reuseDropsIncompatibleCandidate());
        assertEquals(1, snapshot.reuseDropsStaleCandidate());
    }

    @Test
    void profilingSnapshotExposesReuseCountersAlongsidePhaseTwelveRequestMetrics() {
        GlobalPathfindingController.PathRequest request = blockRequest(new BlockPos(0, 0, 0), new BlockPos(3, 0, 3), 20);

        GlobalPathfindingController.requestPath(request, null, () -> createPath(new BlockPos(2, 0, 2), new BlockPos(3, 0, 3)));
        GlobalPathfindingController.requestPath(request, null, () -> createPath(new BlockPos(9, 0, 9), new BlockPos(10, 0, 10)));

        GlobalPathfindingController.resetProfiling();

        GlobalPathfindingController.ProfilingSnapshot snapshot = GlobalPathfindingController.profilingSnapshot();

        assertEquals(0, snapshot.totalRequests());
        assertEquals(0, snapshot.blockTargetRequests());
        assertEquals(0, snapshot.entityTargetRequests());
        assertEquals(0, snapshot.asyncEnabledRequests());
        assertEquals(0, snapshot.asyncDisabledRequests());
        assertEquals(0, snapshot.targetPositionsObserved());
        assertEquals(0, snapshot.reuseAttempts());
        assertEquals(0, snapshot.reuseHits());
        assertEquals(0, snapshot.reuseMisses());
        assertEquals(0, snapshot.reuseMissesNoCandidate());
        assertEquals(0, snapshot.reuseDropsNullCandidate());
        assertEquals(0, snapshot.reuseDropsUnprocessedCandidate());
        assertEquals(0, snapshot.reuseDropsDoneCandidate());
        assertEquals(0, snapshot.reuseDropsIncompatibleCandidate());
        assertEquals(0, snapshot.reuseDropsStaleCandidate());
        assertEquals(0, snapshot.flowFieldEligibleRequests());
        assertEquals(0, snapshot.flowFieldPrototypeAttempts());
        assertEquals(0, snapshot.flowFieldPrototypeHits());
        assertEquals(0, snapshot.flowFieldPrototypeFallbacks());
        assertEquals(0, snapshot.requestBudgetPerTick());
        assertEquals(0, snapshot.budgetUsedThisTick());
        assertEquals(0, snapshot.deferredRequests());
        assertEquals(0, snapshot.deferredResumes());
        assertEquals(0, snapshot.deferredDrops());
        assertEquals(0, snapshot.deferredDropsBacklogCap());
        assertEquals(0, snapshot.deferredDropsMaxAge());
        assertEquals(0, snapshot.deferredDropsInvalidated());
        assertEquals(0, snapshot.currentDeferredQueueDepth());
        assertEquals(0, snapshot.maxDeferredQueueDepth());
        assertEquals(0, snapshot.totalDeferredLatencyTicks());
        assertEquals(0, snapshot.maxDeferredLatencyTicks());
    }

    @Test
    void budgetExhaustionDefersThenResumesWhenLaterTickHasCapacity() {
        GlobalPathfindingController.configureBudgetForTests(1, 4, 20);

        GlobalPathfindingController.PathRequest firstRequest = blockRequest(new BlockPos(0, 0, 0), new BlockPos(4, 0, 4), 100L);
        GlobalPathfindingController.PathRequest secondRequest = blockRequest(new BlockPos(1, 0, 0), new BlockPos(20, 0, 20), 100L);
        GlobalPathfindingController.PathRequest resumedRequest = blockRequest(new BlockPos(1, 0, 0), new BlockPos(20, 0, 20), 103L);
        AtomicInteger supplierCalls = new AtomicInteger();

        GlobalPathfindingController.PathRequestResult<Path> firstResult = GlobalPathfindingController.requestPath(firstRequest, null, () -> {
            supplierCalls.incrementAndGet();
            return createPath(new BlockPos(2, 0, 2), new BlockPos(4, 0, 4));
        });

        GlobalPathfindingController.PathRequestResult<Path> deferredResult = GlobalPathfindingController.requestPath(secondRequest, null, () -> {
            supplierCalls.incrementAndGet();
            return createPath(new BlockPos(3, 0, 3), new BlockPos(20, 0, 20));
        });

        GlobalPathfindingController.PathRequestResult<Path> resumedResult = GlobalPathfindingController.requestPath(
                resumedRequest,
                deferredResult.deferredTicket(),
                () -> {
                    supplierCalls.incrementAndGet();
                    return createPath(new BlockPos(3, 0, 3), new BlockPos(20, 0, 20));
                }
        );

        GlobalPathfindingController.ProfilingSnapshot snapshot = GlobalPathfindingController.profilingSnapshot();

        assertEquals(GlobalPathfindingController.RequestStatus.EXECUTED, firstResult.status());
        assertEquals(GlobalPathfindingController.RequestStatus.DEFERRED, deferredResult.status());
        assertEquals(GlobalPathfindingController.RequestStatus.EXECUTED, resumedResult.status());
        assertNotNull(deferredResult.deferredTicket());
        assertEquals(2, supplierCalls.get());
        assertEquals(1, snapshot.requestBudgetPerTick());
        assertEquals(1, snapshot.budgetUsedThisTick());
        assertEquals(1, snapshot.deferredRequests());
        assertEquals(1, snapshot.deferredResumes());
        assertEquals(0, snapshot.deferredDrops());
        assertEquals(0, snapshot.currentDeferredQueueDepth());
        assertEquals(1, snapshot.maxDeferredQueueDepth());
        assertEquals(3, snapshot.totalDeferredLatencyTicks());
        assertEquals(3, snapshot.maxDeferredLatencyTicks());
    }

    @Test
    void backlogAndInvalidationDropsAreCountedExplicitly() {
        GlobalPathfindingController.configureBudgetForTests(0, 1, 20);

        GlobalPathfindingController.PathRequest firstDeferredRequest = blockRequest(new BlockPos(0, 0, 0), new BlockPos(2, 0, 2), 200L);
        GlobalPathfindingController.PathRequest secondDeferredRequest = blockRequest(new BlockPos(1, 0, 0), new BlockPos(3, 0, 3), 200L);

        GlobalPathfindingController.PathRequestResult<Path> deferredResult = GlobalPathfindingController.requestPath(firstDeferredRequest, null, () -> createPath(new BlockPos(2, 0, 2)));
        GlobalPathfindingController.PathRequestResult<Path> droppedForBacklog = GlobalPathfindingController.requestPath(secondDeferredRequest, null, () -> createPath(new BlockPos(3, 0, 3)));

        assertEquals(GlobalPathfindingController.RequestStatus.DEFERRED, deferredResult.status());
        assertEquals(GlobalPathfindingController.RequestStatus.DROPPED, droppedForBacklog.status());
        assertEquals(GlobalPathfindingController.DeferredDropReason.BACKLOG_CAP, droppedForBacklog.dropReason());

        GlobalPathfindingController.discardDeferred(
                deferredResult.deferredTicket(),
                201L,
                GlobalPathfindingController.DeferredDropReason.INVALIDATED
        );

        GlobalPathfindingController.ProfilingSnapshot snapshot = GlobalPathfindingController.profilingSnapshot();

        assertEquals(1, snapshot.deferredRequests());
        assertEquals(2, snapshot.deferredDrops());
        assertEquals(1, snapshot.deferredDropsBacklogCap());
        assertEquals(1, snapshot.deferredDropsInvalidated());
        assertEquals(0, snapshot.currentDeferredQueueDepth());
        assertEquals(1, snapshot.maxDeferredQueueDepth());
    }

    @Test
    void expiredDeferredRequestsAreDroppedBeforeRetry() {
        GlobalPathfindingController.configureBudgetForTests(0, 4, 2);

        GlobalPathfindingController.PathRequest initialRequest = blockRequest(new BlockPos(0, 0, 0), new BlockPos(2, 0, 2), 300L);
        GlobalPathfindingController.PathRequest retryRequest = blockRequest(new BlockPos(0, 0, 0), new BlockPos(2, 0, 2), 303L);

        GlobalPathfindingController.PathRequestResult<Path> deferredResult = GlobalPathfindingController.requestPath(initialRequest, null, () -> createPath(new BlockPos(2, 0, 2)));
        GlobalPathfindingController.PathRequestResult<Path> retryResult = GlobalPathfindingController.requestPath(
                retryRequest,
                deferredResult.deferredTicket(),
                () -> createPath(new BlockPos(2, 0, 2))
        );

        GlobalPathfindingController.ProfilingSnapshot snapshot = GlobalPathfindingController.profilingSnapshot();

        assertEquals(GlobalPathfindingController.RequestStatus.DEFERRED, deferredResult.status());
        assertEquals(GlobalPathfindingController.RequestStatus.DROPPED, retryResult.status());
        assertEquals(GlobalPathfindingController.DeferredDropReason.INVALIDATED, retryResult.dropReason());
        assertEquals(1, snapshot.deferredRequests());
        assertEquals(1, snapshot.deferredDrops());
        assertEquals(1, snapshot.deferredDropsMaxAge());
        assertFalse(snapshot.currentDeferredQueueDepth() > 0);
    }

    @Test
    void eligibleFlowFieldRequestFallsBackCleanlyWhenNoPrototypeCandidateExists() {
        GlobalPathfindingController.PathRequest request = blockRequest(
                new BlockPos(0, 0, 0),
                new BlockPos(8, 0, 0),
                40L,
                new GlobalPathfindingController.FlowFieldPrototypeRequest(true, true, 6)
        );
        AtomicInteger supplierCalls = new AtomicInteger();

        GlobalPathfindingController.PathRequestResult<Path> result = GlobalPathfindingController.requestPath(
                request,
                null,
                () -> {
                    supplierCalls.incrementAndGet();
                    return createPath(new BlockPos(1, 0, 0), new BlockPos(8, 0, 0));
                }
        );

        GlobalPathfindingController.ProfilingSnapshot snapshot = GlobalPathfindingController.profilingSnapshot();

        assertEquals(GlobalPathfindingController.RequestStatus.EXECUTED, result.status());
        assertEquals(1, supplierCalls.get());
        assertEquals(1, snapshot.flowFieldEligibleRequests());
        assertEquals(1, snapshot.flowFieldPrototypeAttempts());
        assertEquals(0, snapshot.flowFieldPrototypeHits());
        assertEquals(1, snapshot.flowFieldPrototypeFallbacks());
    }

    @Test
    void eligibleFlowFieldRequestCanReuseSharedPathSuffixWithoutCallingSupplier() {
        GlobalPathfindingController.PathRequest seedingRequest = blockRequest(
                new BlockPos(0, 0, 0),
                new BlockPos(8, 0, 0),
                60L,
                new GlobalPathfindingController.FlowFieldPrototypeRequest(false, false, 0)
        );
        GlobalPathfindingController.PathRequest prototypeRequest = blockRequest(
                new BlockPos(2, 0, 0),
                new BlockPos(8, 0, 0),
                61L,
                new GlobalPathfindingController.FlowFieldPrototypeRequest(true, true, 6)
        );
        AtomicInteger supplierCalls = new AtomicInteger();

        GlobalPathfindingController.requestPath(
                seedingRequest,
                null,
                () -> createPath(new BlockPos(0, 0, 0), new BlockPos(2, 0, 0), new BlockPos(5, 0, 0), new BlockPos(8, 0, 0))
        );

        GlobalPathfindingController.PathRequestResult<Path> result = GlobalPathfindingController.requestPath(
                prototypeRequest,
                null,
                () -> {
                    supplierCalls.incrementAndGet();
                    return createPath(new BlockPos(9, 0, 0));
                }
        );

        GlobalPathfindingController.ProfilingSnapshot snapshot = GlobalPathfindingController.profilingSnapshot();

        assertEquals(GlobalPathfindingController.RequestStatus.EXECUTED, result.status());
        assertNotNull(result.result());
        assertEquals(0, supplierCalls.get());
        assertEquals(1, snapshot.flowFieldEligibleRequests());
        assertEquals(1, snapshot.flowFieldPrototypeAttempts());
        assertEquals(1, snapshot.flowFieldPrototypeHits());
        assertEquals(0, snapshot.flowFieldPrototypeFallbacks());
    }

    private static GlobalPathfindingController.PathRequest blockRequest(BlockPos requesterPos, BlockPos targetPos, long gameTime) {
        return blockRequest(requesterPos, targetPos, gameTime, null);
    }

    private static GlobalPathfindingController.PathRequest blockRequest(BlockPos requesterPos, BlockPos targetPos, long gameTime,
                                                                        GlobalPathfindingController.FlowFieldPrototypeRequest flowFieldPrototypeRequest) {
        return new GlobalPathfindingController.PathRequest(
                GlobalPathfindingController.RequestKind.BLOCK_TARGETS,
                true,
                1,
                gameTime,
                new GlobalPathfindingController.ReuseContext(requesterPos, targetPos, gameTime, 4, 2, 20),
                flowFieldPrototypeRequest
        );
    }

    private static GlobalPathfindingController.PathRequest entityRequest(BlockPos requesterPos, BlockPos targetPos, long gameTime) {
        return new GlobalPathfindingController.PathRequest(
                GlobalPathfindingController.RequestKind.ENTITY_TARGET,
                true,
                1,
                gameTime,
                new GlobalPathfindingController.ReuseContext(requesterPos, targetPos, gameTime, 4, 2, 20)
        );
    }

    private static Path createPath(BlockPos... nodes) {
        assertTrue(nodes.length >= 1);
        List<Node> pathNodes = new java.util.ArrayList<>();
        for (BlockPos pos : nodes) {
            pathNodes.add(new Node(pos.getX(), pos.getY(), pos.getZ()));
        }
        return new Path(pathNodes, nodes[nodes.length - 1], true);
    }
}
