package com.talhanation.bannermod.ai.pathfinding.async;

public record DynamicObstacleSnapshot(
        double minX,
        double minY,
        double minZ,
        double maxX,
        double maxY,
        double maxZ,
        boolean blocking,
        int additionalCost
) {
    public DynamicObstacleSnapshot {
        if (maxX < minX || maxY < minY || maxZ < minZ) {
            throw new IllegalArgumentException("Obstacle bounds are invalid");
        }
    }
}
