package com.talhanation.bannermod.ai.pathfinding.async;

public enum PathResultStatus {
    SUCCESS,
    PARTIAL,
    NO_PATH,
    CANCELLED,
    DEADLINE_EXCEEDED,
    INVALID_SNAPSHOT,
    UNSUPPORTED
}
