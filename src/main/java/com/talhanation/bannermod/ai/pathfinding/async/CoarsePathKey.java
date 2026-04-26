package com.talhanation.bannermod.ai.pathfinding.async;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import java.util.Objects;

public record CoarsePathKey(
        String dimensionId,
        ChunkPos startChunk,
        ChunkPos targetChunk,
        PathPriority priority
) {
    public CoarsePathKey {
        Objects.requireNonNull(dimensionId, "dimensionId");
        Objects.requireNonNull(startChunk, "startChunk");
        Objects.requireNonNull(targetChunk, "targetChunk");
        Objects.requireNonNull(priority, "priority");
    }

    public static CoarsePathKey of(String dimensionId, BlockPos start, BlockPos target, PathPriority priority) {
        return new CoarsePathKey(
                dimensionId,
                new ChunkPos(start),
                new ChunkPos(target),
                priority
        );
    }
}
