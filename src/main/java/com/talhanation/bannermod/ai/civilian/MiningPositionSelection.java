package com.talhanation.bannermod.ai.civilian;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.Stack;

public final class MiningPositionSelection {

    private MiningPositionSelection() {
    }

    public static BlockPos popNearest(Stack<BlockPos> positions, Vec3 origin) {
        if (positions == null || positions.isEmpty() || origin == null) {
            return null;
        }

        BlockPos nearest = positions.stream()
                .min(Comparator.comparingDouble(pos -> origin.distanceToSqr(pos.getCenter())))
                .orElse(null);
        if (nearest != null) {
            positions.remove(nearest);
        }
        return nearest;
    }
}
