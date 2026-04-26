package com.talhanation.bannermod.ai.pathfinding.async;

public interface AsyncPathSolver {
    PathResult solve(PathRequestSnapshot request, RegionSnapshot region, CancellationToken cancellationToken);
}
