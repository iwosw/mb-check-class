package com.talhanation.bannermod.ai.pathfinding.async;

import java.util.Objects;

public record SnapshotBuildResult(
        PathRequestSnapshot request,
        RegionSnapshot region,
        SnapshotStatus status,
        long buildNanos
) {
    public SnapshotBuildResult {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(status, "status");
    }
}
