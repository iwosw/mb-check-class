package com.talhanation.bannermod.ai.pathfinding.async;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsyncPathSchedulerTest {

    @Test
    void schedulerRejectsWhenGlobalQueueCapReached() {
        AsyncPathSolver slowSolver = (request, region, cancellationToken) -> {
            try {
                Thread.sleep(80L);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            return success(request, List.of(request.start(), request.targets().get(0)));
        };
        Map<PathPriority, Integer> caps = new EnumMap<>(PathPriority.class);
        caps.put(PathPriority.WANDER, 2);
        caps.put(PathPriority.WORK, 2);
        caps.put(PathPriority.FOLLOW, 2);
        caps.put(PathPriority.COMBAT, 2);

        try (AsyncPathScheduler scheduler = new AsyncPathScheduler(slowSolver, 1, 1, caps)) {
            boolean first = scheduler.submit(request(PathPriority.WORK, 1L), openRegion(), CancellationToken.NONE);
            boolean canAcceptAfterFirst = scheduler.canAccept(PathPriority.WORK);
            boolean second = scheduler.submit(request(PathPriority.WORK, 2L), openRegion(), CancellationToken.NONE);

            assertTrue(first);
            assertFalse(canAcceptAfterFirst);
            assertFalse(second);
        }
    }

    @Test
    void schedulerRejectsWhenPriorityCapReached() {
        AsyncPathSolver slowSolver = (request, region, cancellationToken) -> {
            try {
                Thread.sleep(80L);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            return success(request, List.of(request.start(), request.targets().get(0)));
        };
        Map<PathPriority, Integer> caps = new EnumMap<>(PathPriority.class);
        caps.put(PathPriority.WANDER, 1);
        caps.put(PathPriority.WORK, 1);
        caps.put(PathPriority.FOLLOW, 1);
        caps.put(PathPriority.COMBAT, 1);

        try (AsyncPathScheduler scheduler = new AsyncPathScheduler(slowSolver, 1, 8, caps)) {
            boolean first = scheduler.submit(request(PathPriority.WORK, 1L), openRegion(), CancellationToken.NONE);
            boolean canAcceptWorkAfterFirst = scheduler.canAccept(PathPriority.WORK);
            boolean canAcceptCombatAfterFirst = scheduler.canAccept(PathPriority.COMBAT);
            boolean second = scheduler.submit(request(PathPriority.WORK, 2L), openRegion(), CancellationToken.NONE);

            assertTrue(first);
            assertFalse(canAcceptWorkAfterFirst);
            assertTrue(canAcceptCombatAfterFirst);
            assertFalse(second);
        }
    }

    @Test
    void schedulerPublishesCompletedResults() throws InterruptedException {
        AsyncPathSolver solver = (request, region, cancellationToken) ->
                success(request, List.of(request.start(), request.targets().get(0)));

        try (AsyncPathScheduler scheduler = new AsyncPathScheduler(solver, 1, 8, defaultCaps())) {
            assertTrue(scheduler.submit(request(PathPriority.COMBAT, 77L), openRegion(), CancellationToken.NONE));

            List<PathResult> completed = List.of();
            for (int i = 0; i < 20 && completed.isEmpty(); i++) {
                Thread.sleep(10L);
                completed = scheduler.pollCompleted(4);
            }

            assertFalse(completed.isEmpty());
            assertEquals(77L, completed.get(0).requestId());
            assertEquals(PathResultStatus.SUCCESS, completed.get(0).status());
        }
    }

    private static Map<PathPriority, Integer> defaultCaps() {
        Map<PathPriority, Integer> caps = new EnumMap<>(PathPriority.class);
        caps.put(PathPriority.WANDER, 8);
        caps.put(PathPriority.WORK, 8);
        caps.put(PathPriority.FOLLOW, 8);
        caps.put(PathPriority.COMBAT, 8);
        return caps;
    }

    private static PathResult success(PathRequestSnapshot request, List<BlockPos> nodes) {
        return new PathResult(
                request.entityUuid(),
                request.requestId(),
                request.epoch(),
                PathResultStatus.SUCCESS,
                nodes,
                true,
                1.0D,
                3,
                1L,
                "ok"
        );
    }

    private static RegionSnapshot openRegion() {
        return new RegionSnapshot(new BlockPos(0, 64, 0), 3, 1, 3, new byte[9], List.of(), false);
    }

    private static PathRequestSnapshot request(PathPriority priority, long requestId) {
        return new PathRequestSnapshot(
                UUID.randomUUID(),
                requestId,
                requestId,
                new BlockPos(0, 64, 0),
                List.of(new BlockPos(2, 64, 2)),
                128,
                64.0F,
                0.6D,
                1.8D,
                0.6D,
                false,
                false,
                false,
                priority,
                10L,
                System.nanoTime() + 1_000_000_000L
        );
    }
}
