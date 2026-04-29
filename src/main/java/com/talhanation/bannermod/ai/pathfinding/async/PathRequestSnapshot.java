package com.talhanation.bannermod.ai.pathfinding.async;

import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Immutable server-thread path request copied before worker-thread solving. */
public record PathRequestSnapshot(
        UUID entityUuid,
        long requestId,
        long epoch,
        BlockPos start,
        List<BlockPos> targets,
        int maxVisitedNodes,
        float maxDistance,
        double agentWidth,
        double agentHeight,
        double stepHeight,
        boolean canFloat,
        boolean canOpenDoors,
        boolean avoidWater,
        PathPriority priority,
        long createdAtGameTime,
        long deadlineNanos
) {
    public PathRequestSnapshot {
        Objects.requireNonNull(entityUuid, "entityUuid");
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(targets, "targets");
        Objects.requireNonNull(priority, "priority");
        if (targets.isEmpty()) {
            throw new IllegalArgumentException("targets must not be empty");
        }
        targets = List.copyOf(targets);
        if (maxVisitedNodes < 0) {
            throw new IllegalArgumentException("maxVisitedNodes must be non-negative");
        }
        if (maxDistance < 0.0F) {
            throw new IllegalArgumentException("maxDistance must be non-negative");
        }
    }
}
