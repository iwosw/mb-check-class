package com.talhanation.bannermod.ai.pathfinding.async;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class RegionSnapshotBuilder {
    private static final int DEFAULT_MARGIN = 8;
    private static final int MAX_AXIS_SIZE = 128;

    public SnapshotBuildResult build(ServerLevel level, PathRequestSnapshot request) {
        Objects.requireNonNull(level, "level");
        return buildWithAccess(request, new ServerLevelWorldAccess(level));
    }

    SnapshotBuildResult buildWithAccess(PathRequestSnapshot request, WorldAccess access) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(access, "access");
        long startedAt = System.nanoTime();

        if (!access.isEntityAlive(request.entityUuid())) {
            return fail(request, SnapshotStatus.ENTITY_GONE, startedAt);
        }
        if (request.agentWidth() <= 0.0D || request.agentHeight() <= 0.0D) {
            return fail(request, SnapshotStatus.UNSUPPORTED_AGENT, startedAt);
        }

        Bounds bounds = computeBounds(request.start(), request.targets(), request.maxDistance());
        if (!bounds.isWithinLimit()) {
            return fail(request, SnapshotStatus.REGION_TOO_LARGE, startedAt);
        }
        if (!access.areChunksLoaded(bounds)) {
            return fail(request, SnapshotStatus.UNLOADED_CHUNK, startedAt);
        }

        int sizeX = bounds.maxX - bounds.minX + 1;
        int sizeY = bounds.maxY - bounds.minY + 1;
        int sizeZ = bounds.maxZ - bounds.minZ + 1;
        byte[] flags = access.buildCellFlags(bounds);
        List<DynamicObstacleSnapshot> dynamicObstacles = access.captureDynamicObstacles(bounds, request.entityUuid());

        RegionSnapshot region = new RegionSnapshot(
                new BlockPos(bounds.minX, bounds.minY, bounds.minZ),
                sizeX,
                sizeY,
                sizeZ,
                flags,
                dynamicObstacles,
                false
        );
        return new SnapshotBuildResult(request, region, SnapshotStatus.OK, System.nanoTime() - startedAt);
    }

    private static SnapshotBuildResult fail(PathRequestSnapshot request, SnapshotStatus status, long startedAt) {
        RegionSnapshot emptyRegion = new RegionSnapshot(
                request.start(),
                1,
                1,
                1,
                new byte[]{0},
                List.of(),
                true
        );
        return new SnapshotBuildResult(request, emptyRegion, status, System.nanoTime() - startedAt);
    }

    private static void fillCellFlags(Level level, Bounds bounds, int sizeX, int sizeY, int sizeZ, byte[] flags) {
        int index = 0;
        for (int y = 0; y < sizeY; y++) {
            int worldY = bounds.minY + y;
            for (int z = 0; z < sizeZ; z++) {
                int worldZ = bounds.minZ + z;
                for (int x = 0; x < sizeX; x++) {
                    int worldX = bounds.minX + x;
                    BlockPos pos = new BlockPos(worldX, worldY, worldZ);
                    BlockState state = level.getBlockState(pos);
                    FluidState fluid = level.getFluidState(pos);
                    BlockState belowState = level.getBlockState(pos.below());
                    boolean solid = !state.getCollisionShape(level, pos).isEmpty();
                    boolean walkableFloor = !belowState.getCollisionShape(level, pos.below()).isEmpty();
                    boolean hasFluid = !fluid.isEmpty();
                    boolean water = fluid.is(FluidTags.WATER);
                    boolean lava = fluid.is(FluidTags.LAVA);
                    boolean doorOpenable = state.is(BlockTags.DOORS);
                    boolean fenceLike = state.is(BlockTags.FENCES) || state.is(BlockTags.WALLS);
                    boolean danger = state.is(Blocks.CACTUS)
                            || state.is(Blocks.MAGMA_BLOCK)
                            || state.is(Blocks.FIRE)
                            || state.is(Blocks.SOUL_FIRE)
                            || state.is(Blocks.SWEET_BERRY_BUSH)
                            || lava;
                    byte cellFlags = 0;
                    if (solid) {
                        cellFlags |= RegionSnapshot.FLAG_SOLID;
                    }
                    if (walkableFloor) {
                        cellFlags |= RegionSnapshot.FLAG_WALKABLE_FLOOR;
                    }
                    if (hasFluid) {
                        cellFlags |= RegionSnapshot.FLAG_FLUID;
                    }
                    if (water) {
                        cellFlags |= RegionSnapshot.FLAG_WATER;
                    }
                    if (lava) {
                        cellFlags |= RegionSnapshot.FLAG_LAVA;
                    }
                    if (doorOpenable) {
                        cellFlags |= RegionSnapshot.FLAG_DOOR_OPENABLE;
                    }
                    if (fenceLike) {
                        cellFlags |= RegionSnapshot.FLAG_FENCE_LIKE;
                    }
                    if (danger) {
                        cellFlags |= RegionSnapshot.FLAG_DANGER;
                    }
                    flags[index++] = cellFlags;
                }
            }
        }
    }

    private static boolean areChunksLoaded(ServerLevel level, Bounds bounds) {
        int minChunkX = bounds.minX >> 4;
        int maxChunkX = bounds.maxX >> 4;
        int minChunkZ = bounds.minZ >> 4;
        int maxChunkZ = bounds.maxZ >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!level.hasChunk(chunkX, chunkZ)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static List<DynamicObstacleSnapshot> captureDynamicObstacles(ServerLevel level, Bounds bounds, UUID requestEntityUuid) {
        AABB queryBox = new AABB(
                bounds.minX, bounds.minY, bounds.minZ,
                bounds.maxX + 1.0D, bounds.maxY + 1.0D, bounds.maxZ + 1.0D
        );
        List<Entity> entities = level.getEntities((Entity) null, queryBox);
        List<DynamicObstacleSnapshot> obstacles = new ArrayList<>(entities.size());
        for (Entity entity : entities) {
            if (entity == null || !entity.isAlive() || requestEntityUuid.equals(entity.getUUID())) {
                continue;
            }
            AABB box = entity.getBoundingBox();
            obstacles.add(new DynamicObstacleSnapshot(
                    box.minX, box.minY, box.minZ,
                    box.maxX, box.maxY, box.maxZ,
                    true,
                    0
            ));
        }
        return List.copyOf(obstacles);
    }

    private static Bounds computeBounds(BlockPos start, List<BlockPos> targets, float maxDistance) {
        int minX = start.getX();
        int minY = start.getY();
        int minZ = start.getZ();
        int maxX = start.getX();
        int maxY = start.getY();
        int maxZ = start.getZ();
        for (BlockPos target : targets) {
            minX = Math.min(minX, target.getX());
            minY = Math.min(minY, target.getY());
            minZ = Math.min(minZ, target.getZ());
            maxX = Math.max(maxX, target.getX());
            maxY = Math.max(maxY, target.getY());
            maxZ = Math.max(maxZ, target.getZ());
        }

        int travelPadding = Math.max(DEFAULT_MARGIN, (int) Math.ceil(maxDistance));
        return new Bounds(
                minX - travelPadding,
                minY - DEFAULT_MARGIN,
                minZ - travelPadding,
                maxX + travelPadding,
                maxY + DEFAULT_MARGIN,
                maxZ + travelPadding
        );
    }

    record Bounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        private boolean isWithinLimit() {
            return (maxX - minX + 1) <= MAX_AXIS_SIZE
                    && (maxY - minY + 1) <= MAX_AXIS_SIZE
                    && (maxZ - minZ + 1) <= MAX_AXIS_SIZE;
        }
    }

    interface WorldAccess {
        boolean isEntityAlive(UUID entityUuid);

        boolean areChunksLoaded(Bounds bounds);

        byte[] buildCellFlags(Bounds bounds);

        List<DynamicObstacleSnapshot> captureDynamicObstacles(Bounds bounds, UUID requestEntityUuid);
    }

    private static final class ServerLevelWorldAccess implements WorldAccess {
        private final ServerLevel level;

        private ServerLevelWorldAccess(ServerLevel level) {
            this.level = level;
        }

        @Override
        public boolean isEntityAlive(UUID entityUuid) {
            Entity entity = level.getEntity(entityUuid);
            return entity != null && entity.isAlive();
        }

        @Override
        public boolean areChunksLoaded(Bounds bounds) {
            return RegionSnapshotBuilder.areChunksLoaded(level, bounds);
        }

        @Override
        public byte[] buildCellFlags(Bounds bounds) {
            int sizeX = bounds.maxX - bounds.minX + 1;
            int sizeY = bounds.maxY - bounds.minY + 1;
            int sizeZ = bounds.maxZ - bounds.minZ + 1;
            byte[] flags = new byte[sizeX * sizeY * sizeZ];
            fillCellFlags(level, bounds, sizeX, sizeY, sizeZ, flags);
            return flags;
        }

        @Override
        public List<DynamicObstacleSnapshot> captureDynamicObstacles(Bounds bounds, UUID requestEntityUuid) {
            return RegionSnapshotBuilder.captureDynamicObstacles(level, bounds, requestEntityUuid);
        }
    }
}
