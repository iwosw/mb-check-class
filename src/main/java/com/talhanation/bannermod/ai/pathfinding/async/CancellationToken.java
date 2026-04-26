package com.talhanation.bannermod.ai.pathfinding.async;

@FunctionalInterface
public interface CancellationToken {
    CancellationToken NONE = () -> false;

    boolean isCancelled();
}
