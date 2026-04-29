package com.talhanation.bannermod.ai.pathfinding.async;

import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Objects;

/** Immutable world slice captured on the server thread and safe to read from async solver workers. */
public record RegionSnapshot(
        BlockPos originMin,
        int sizeX,
        int sizeY,
        int sizeZ,
        byte[] cellFlags,
        List<DynamicObstacleSnapshot> dynamicObstacles,
        boolean invalidRegion
) {
    public static final byte FLAG_SOLID = 1 << 0;
    public static final byte FLAG_WALKABLE_FLOOR = 1 << 1;
    public static final byte FLAG_FLUID = 1 << 2;
    public static final byte FLAG_WATER = 1 << 3;
    public static final byte FLAG_LAVA = 1 << 4;
    public static final byte FLAG_DOOR_OPENABLE = 1 << 5;
    public static final byte FLAG_FENCE_LIKE = 1 << 6;
    public static final byte FLAG_DANGER = (byte) (1 << 7);

    public RegionSnapshot {
        Objects.requireNonNull(originMin, "originMin");
        Objects.requireNonNull(cellFlags, "cellFlags");
        Objects.requireNonNull(dynamicObstacles, "dynamicObstacles");
        if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0) {
            throw new IllegalArgumentException("Region sizes must be positive");
        }
        long expectedSize = (long) sizeX * sizeY * sizeZ;
        if (expectedSize > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Region is too large for compact indexing");
        }
        if (cellFlags.length != (int) expectedSize) {
            throw new IllegalArgumentException("cellFlags length does not match region volume");
        }
        cellFlags = cellFlags.clone();
        dynamicObstacles = List.copyOf(dynamicObstacles);
    }

    public int indexOf(int localX, int localY, int localZ) {
        if (localX < 0 || localX >= sizeX || localY < 0 || localY >= sizeY || localZ < 0 || localZ >= sizeZ) {
            throw new IndexOutOfBoundsException("Cell coordinate is outside region bounds");
        }
        return (localY * sizeZ + localZ) * sizeX + localX;
    }

    public byte flagsAt(int localX, int localY, int localZ) {
        return cellFlags[indexOf(localX, localY, localZ)];
    }

    public boolean hasFlag(int localX, int localY, int localZ, byte flagMask) {
        return (flagsAt(localX, localY, localZ) & flagMask) != 0;
    }

    public int movementCostAt(int localX, int localY, int localZ) {
        return movementCostForFlags(flagsAt(localX, localY, localZ));
    }

    public static int movementCostForFlags(byte flags) {
        int cost = 1;
        if ((flags & FLAG_FLUID) != 0) {
            cost += 2;
        }
        if ((flags & FLAG_WATER) != 0) {
            cost += 1;
        }
        if ((flags & FLAG_DOOR_OPENABLE) != 0) {
            cost += 1;
        }
        if ((flags & FLAG_FENCE_LIKE) != 0) {
            cost += 3;
        }
        if ((flags & FLAG_DANGER) != 0) {
            cost += 4;
        }
        return cost;
    }
}
