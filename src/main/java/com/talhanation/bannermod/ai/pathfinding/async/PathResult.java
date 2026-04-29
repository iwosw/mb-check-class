package com.talhanation.bannermod.ai.pathfinding.async;

import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Immutable worker-thread result; committers must inspect status before applying nodes on the server thread. */
public record PathResult(
        UUID entityUuid,
        long requestId,
        long epoch,
        PathResultStatus status,
        List<BlockPos> nodes,
        boolean reached,
        double cost,
        int visitedNodes,
        long solveNanos,
        String debugReason
) {
    public PathResult {
        Objects.requireNonNull(entityUuid, "entityUuid");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(nodes, "nodes");
        debugReason = debugReason == null ? "" : debugReason;
        nodes = List.copyOf(nodes);
        if (visitedNodes < 0) {
            throw new IllegalArgumentException("visitedNodes must be non-negative");
        }
        if (solveNanos < 0L) {
            throw new IllegalArgumentException("solveNanos must be non-negative");
        }
    }
}
