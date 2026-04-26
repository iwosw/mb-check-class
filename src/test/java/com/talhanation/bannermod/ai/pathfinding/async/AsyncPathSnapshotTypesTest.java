package com.talhanation.bannermod.ai.pathfinding.async;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsyncPathSnapshotTypesTest {

    @Test
    void pathRequestSnapshotMakesDefensiveCopyOfTargets() {
        List<BlockPos> sourceTargets = new ArrayList<>();
        sourceTargets.add(new BlockPos(1, 64, 1));

        PathRequestSnapshot snapshot = new PathRequestSnapshot(
                UUID.randomUUID(),
                10L,
                1L,
                new BlockPos(0, 64, 0),
                sourceTargets,
                512,
                24.0F,
                0.6D,
                1.8D,
                0.6D,
                false,
                false,
                false,
                PathPriority.WORK,
                100L,
                1_000_000L
        );

        sourceTargets.add(new BlockPos(2, 64, 2));

        assertEquals(1, snapshot.targets().size());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.targets().add(new BlockPos(3, 64, 3)));
    }

    @Test
    void pathRequestSnapshotRejectsEmptyTargets() {
        assertThrows(IllegalArgumentException.class, () -> new PathRequestSnapshot(
                UUID.randomUUID(),
                10L,
                1L,
                new BlockPos(0, 64, 0),
                List.of(),
                512,
                24.0F,
                0.6D,
                1.8D,
                0.6D,
                false,
                false,
                false,
                PathPriority.WORK,
                100L,
                1_000_000L
        ));
    }

    @Test
    void dynamicObstacleSnapshotRejectsInvalidBounds() {
        assertThrows(IllegalArgumentException.class, () -> new DynamicObstacleSnapshot(
                10.0D, 5.0D, 10.0D,
                9.0D, 6.0D, 11.0D,
                true,
                0
        ));
    }

    @Test
    void regionSnapshotMakesDefensiveCopies() {
        byte[] flags = new byte[]{1, 0, 1, 0};
        List<DynamicObstacleSnapshot> obstacles = new ArrayList<>();
        obstacles.add(new DynamicObstacleSnapshot(0.0D, 64.0D, 0.0D, 1.0D, 65.0D, 1.0D, true, 0));

        RegionSnapshot snapshot = new RegionSnapshot(
                new BlockPos(0, 64, 0),
                2,
                1,
                2,
                flags,
                obstacles,
                false
        );

        flags[0] = 0;
        obstacles.clear();

        assertEquals(1, snapshot.cellFlags()[0]);
        assertEquals(1, snapshot.dynamicObstacles().size());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.dynamicObstacles().add(
                new DynamicObstacleSnapshot(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D, true, 0)
        ));
    }

    @Test
    void regionSnapshotValidatesVolumeAgainstFlagLength() {
        assertThrows(IllegalArgumentException.class, () -> new RegionSnapshot(
                new BlockPos(0, 64, 0),
                2,
                2,
                1,
                new byte[]{1, 0, 1},
                List.of(),
                false
        ));
    }

    @Test
    void snapshotBuildResultAcceptsMinimalValidData() {
        PathRequestSnapshot request = new PathRequestSnapshot(
                UUID.randomUUID(),
                1L,
                1L,
                new BlockPos(0, 64, 0),
                List.of(new BlockPos(1, 64, 1)),
                64,
                16.0F,
                0.6D,
                1.8D,
                0.6D,
                false,
                false,
                false,
                PathPriority.WANDER,
                0L,
                1000L
        );
        RegionSnapshot region = new RegionSnapshot(
                new BlockPos(0, 64, 0),
                1,
                1,
                1,
                new byte[]{0},
                List.of(),
                false
        );

        SnapshotBuildResult result = assertDoesNotThrow(() -> new SnapshotBuildResult(
                request,
                region,
                SnapshotStatus.OK,
                10L
        ));

        assertNotNull(result);
        assertEquals(SnapshotStatus.OK, result.status());
        assertTrue(result.buildNanos() >= 0L);
    }

    @Test
    void regionSnapshotSupportsFlagLookupAndDerivedMovementCost() {
        byte mixedFlags = 0;
        mixedFlags |= RegionSnapshot.FLAG_FLUID;
        mixedFlags |= RegionSnapshot.FLAG_WATER;
        mixedFlags |= RegionSnapshot.FLAG_DOOR_OPENABLE;
        mixedFlags |= RegionSnapshot.FLAG_DANGER;
        RegionSnapshot snapshot = new RegionSnapshot(
                new BlockPos(0, 64, 0),
                1,
                1,
                1,
                new byte[]{mixedFlags},
                List.of(),
                false
        );

        assertTrue(snapshot.hasFlag(0, 0, 0, RegionSnapshot.FLAG_FLUID));
        assertTrue(snapshot.hasFlag(0, 0, 0, RegionSnapshot.FLAG_WATER));
        assertEquals(9, snapshot.movementCostAt(0, 0, 0));
    }
}
