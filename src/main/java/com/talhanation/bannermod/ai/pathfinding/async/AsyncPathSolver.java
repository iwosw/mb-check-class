package com.talhanation.bannermod.ai.pathfinding.async;

/**
 * Worker-thread path solver over immutable request/snapshot data only.
 * Implementations must not read live Level/entity state; the server thread owns snapshot capture and result commit.
 */
public interface AsyncPathSolver {
    PathResult solve(PathRequestSnapshot request, RegionSnapshot region, CancellationToken cancellationToken);
}
