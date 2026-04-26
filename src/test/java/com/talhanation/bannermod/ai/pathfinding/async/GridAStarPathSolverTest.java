package com.talhanation.bannermod.ai.pathfinding.async;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GridAStarPathSolverTest {

    @Test
    void solverFindsPathOnSyntheticSnapshot() {
        RegionSnapshot region = openRegion(5, 1, 5);
        PathRequestSnapshot request = request(new BlockPos(0, 64, 0), new BlockPos(4, 64, 4), 512, futureDeadline());

        PathResult result = new GridAStarPathSolver().solve(request, region, CancellationToken.NONE);

        assertEquals(PathResultStatus.SUCCESS, result.status());
        assertTrue(result.reached());
        assertFalse(result.nodes().isEmpty());
        assertEquals(new BlockPos(0, 64, 0), result.nodes().get(0));
        assertEquals(new BlockPos(4, 64, 4), result.nodes().get(result.nodes().size() - 1));
    }

    @Test
    void solverReturnsNoPathWhenCorridorIsBlocked() {
        byte[] flags = new byte[5];
        flags[1] = RegionSnapshot.FLAG_SOLID;
        flags[2] = RegionSnapshot.FLAG_SOLID;
        flags[3] = RegionSnapshot.FLAG_SOLID;
        RegionSnapshot region = new RegionSnapshot(new BlockPos(0, 64, 0), 5, 1, 1, flags, List.of(), false);
        PathRequestSnapshot request = request(new BlockPos(0, 64, 0), new BlockPos(4, 64, 0), 128, futureDeadline());

        PathResult result = new GridAStarPathSolver().solve(request, region, CancellationToken.NONE);

        assertEquals(PathResultStatus.NO_PATH, result.status());
        assertFalse(result.reached());
    }

    @Test
    void solverReturnsCancelledWhenTokenCancelledBeforeSolve() {
        RegionSnapshot region = openRegion(3, 1, 3);
        PathRequestSnapshot request = request(new BlockPos(0, 64, 0), new BlockPos(2, 64, 2), 128, futureDeadline());

        PathResult result = new GridAStarPathSolver().solve(request, region, () -> true);

        assertEquals(PathResultStatus.CANCELLED, result.status());
        assertFalse(result.reached());
    }

    @Test
    void solverReturnsDeadlineExceededForPastDeadline() {
        RegionSnapshot region = openRegion(3, 1, 3);
        PathRequestSnapshot request = request(new BlockPos(0, 64, 0), new BlockPos(2, 64, 2), 128, System.nanoTime() - 1L);

        PathResult result = new GridAStarPathSolver().solve(request, region, CancellationToken.NONE);

        assertEquals(PathResultStatus.DEADLINE_EXCEEDED, result.status());
        assertFalse(result.reached());
    }

    private static RegionSnapshot openRegion(int sizeX, int sizeY, int sizeZ) {
        byte[] flags = new byte[sizeX * sizeY * sizeZ];
        return new RegionSnapshot(new BlockPos(0, 64, 0), sizeX, sizeY, sizeZ, flags, List.of(), false);
    }

    private static PathRequestSnapshot request(BlockPos start, BlockPos target, int maxVisitedNodes, long deadlineNanos) {
        return new PathRequestSnapshot(
                UUID.randomUUID(),
                11L,
                3L,
                start,
                List.of(target),
                maxVisitedNodes,
                64.0F,
                0.6D,
                1.8D,
                0.6D,
                false,
                false,
                false,
                PathPriority.WORK,
                100L,
                deadlineNanos
        );
    }

    private static long futureDeadline() {
        return System.nanoTime() + 1_000_000_000L;
    }
}
