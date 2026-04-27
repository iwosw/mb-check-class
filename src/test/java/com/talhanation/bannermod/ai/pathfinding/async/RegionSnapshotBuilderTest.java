package com.talhanation.bannermod.ai.pathfinding.async;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionSnapshotBuilderTest {

    @Test
    void returnsEntityGoneWhenEntityIsMissingOrDead() {
        RegionSnapshotBuilder builder = new RegionSnapshotBuilder();
        PathRequestSnapshot request = validRequest(16.0F, 0.6D, 1.8D);
        FakeWorldAccess access = new FakeWorldAccess(false, true, List.of());

        SnapshotBuildResult result = builder.buildWithAccess(request, access);

        assertEquals(SnapshotStatus.ENTITY_GONE, result.status());
        assertTrue(result.region().invalidRegion());
    }

    @Test
    void returnsUnsupportedAgentWhenAgentDimensionsAreInvalid() {
        RegionSnapshotBuilder builder = new RegionSnapshotBuilder();
        PathRequestSnapshot request = validRequest(16.0F, 0.0D, 1.8D);
        FakeWorldAccess access = new FakeWorldAccess(true, true, List.of());

        SnapshotBuildResult result = builder.buildWithAccess(request, access);

        assertEquals(SnapshotStatus.UNSUPPORTED_AGENT, result.status());
        assertTrue(result.region().invalidRegion());
    }

    @Test
    void returnsRegionTooLargeWhenBoundsExceedClamp() {
        RegionSnapshotBuilder builder = new RegionSnapshotBuilder();
        PathRequestSnapshot request = validRequest(200.0F, 0.6D, 1.8D);
        FakeWorldAccess access = new FakeWorldAccess(true, true, List.of());

        SnapshotBuildResult result = builder.buildWithAccess(request, access);

        assertEquals(SnapshotStatus.REGION_TOO_LARGE, result.status());
        assertTrue(result.region().invalidRegion());
    }

    @Test
    void returnsUnloadedChunkWhenWorldAccessReportsMissingChunks() {
        RegionSnapshotBuilder builder = new RegionSnapshotBuilder();
        PathRequestSnapshot request = validRequest(16.0F, 0.6D, 1.8D);
        FakeWorldAccess access = new FakeWorldAccess(true, false, List.of());

        SnapshotBuildResult result = builder.buildWithAccess(request, access);

        assertEquals(SnapshotStatus.UNLOADED_CHUNK, result.status());
        assertTrue(result.region().invalidRegion());
    }

    @Test
    void returnsOkAndKeepsDynamicObstaclesForValidInputs() {
        RegionSnapshotBuilder builder = new RegionSnapshotBuilder();
        PathRequestSnapshot request = validRequest(16.0F, 0.6D, 1.8D);
        DynamicObstacleSnapshot obstacle = new DynamicObstacleSnapshot(1.0D, 64.0D, 1.0D, 2.0D, 65.0D, 2.0D, true, 0);
        FakeWorldAccess access = new FakeWorldAccess(
                true,
                true,
                List.of(obstacle),
                (byte) (RegionSnapshot.FLAG_FLUID | RegionSnapshot.FLAG_DANGER)
        );

        SnapshotBuildResult result = builder.buildWithAccess(request, access);

        assertEquals(SnapshotStatus.OK, result.status());
        assertFalse(result.region().invalidRegion());
        assertEquals(1, result.region().dynamicObstacles().size());
        assertEquals(obstacle, result.region().dynamicObstacles().get(0));
        assertEquals(expectedVolume(16.0F), result.region().cellFlags().length);
        assertEquals(7, result.region().movementCostAt(0, 0, 0));
    }

    @Test
    void returnsBudgetExceededWhenSnapshotWorkPassesBudget() {
        RegionSnapshotBuilder builder = new RegionSnapshotBuilder();
        PathRequestSnapshot request = validRequest(16.0F, 0.6D, 1.8D);
        FakeWorldAccess access = new FakeWorldAccess(true, true, List.of(), (byte) 0, 5L);

        SnapshotBuildResult result = builder.buildWithAccess(request, access, 1L);

        assertEquals(SnapshotStatus.BUDGET_EXCEEDED, result.status());
        assertTrue(result.region().invalidRegion());
    }

    private static PathRequestSnapshot validRequest(float maxDistance, double width, double height) {
        return new PathRequestSnapshot(
                UUID.randomUUID(),
                1L,
                1L,
                new BlockPos(0, 64, 0),
                List.of(new BlockPos(4, 64, 4)),
                256,
                maxDistance,
                width,
                height,
                0.6D,
                false,
                false,
                false,
                PathPriority.FOLLOW,
                100L,
                1_000_000L
        );
    }

    private static int expectedVolume(float maxDistance) {
        int travelPadding = Math.max(8, (int) Math.ceil(maxDistance));
        int sizeX = (4 + travelPadding) - (0 - travelPadding) + 1;
        int sizeY = (64 + 8) - (64 - 8) + 1;
        int sizeZ = (4 + travelPadding) - (0 - travelPadding) + 1;
        return sizeX * sizeY * sizeZ;
    }

    private static final class FakeWorldAccess implements RegionSnapshotBuilder.WorldAccess {
        private final boolean entityAlive;
        private final boolean chunksLoaded;
        private final List<DynamicObstacleSnapshot> obstacles;
        private final byte fillFlag;
        private final long buildDelayMillis;

        private FakeWorldAccess(boolean entityAlive, boolean chunksLoaded, List<DynamicObstacleSnapshot> obstacles) {
            this(entityAlive, chunksLoaded, obstacles, (byte) 0);
        }

        private FakeWorldAccess(boolean entityAlive, boolean chunksLoaded, List<DynamicObstacleSnapshot> obstacles, byte fillFlag) {
            this(entityAlive, chunksLoaded, obstacles, fillFlag, 0L);
        }

        private FakeWorldAccess(boolean entityAlive, boolean chunksLoaded, List<DynamicObstacleSnapshot> obstacles, byte fillFlag, long buildDelayMillis) {
            this.entityAlive = entityAlive;
            this.chunksLoaded = chunksLoaded;
            this.obstacles = obstacles;
            this.fillFlag = fillFlag;
            this.buildDelayMillis = buildDelayMillis;
        }

        @Override
        public boolean isEntityAlive(UUID entityUuid) {
            return entityAlive;
        }

        @Override
        public boolean areChunksLoaded(RegionSnapshotBuilder.Bounds bounds) {
            return chunksLoaded;
        }

        @Override
        public byte[] buildCellFlags(RegionSnapshotBuilder.Bounds bounds) {
            if (buildDelayMillis > 0L) {
                try {
                    Thread.sleep(buildDelayMillis);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
            int sizeX = bounds.maxX() - bounds.minX() + 1;
            int sizeY = bounds.maxY() - bounds.minY() + 1;
            int sizeZ = bounds.maxZ() - bounds.minZ() + 1;
            byte[] data = new byte[sizeX * sizeY * sizeZ];
            java.util.Arrays.fill(data, fillFlag);
            return data;
        }

        @Override
        public List<DynamicObstacleSnapshot> captureDynamicObstacles(RegionSnapshotBuilder.Bounds bounds, UUID requestEntityUuid) {
            return obstacles;
        }
    }
}
