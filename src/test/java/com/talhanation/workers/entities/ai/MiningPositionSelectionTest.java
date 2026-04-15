package com.talhanation.workers.entities.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.Stack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class MiningPositionSelectionTest {

    @Test
    void popsNearestPositionWithoutNaturalOrderBias() {
        Stack<BlockPos> positions = new Stack<>();
        positions.add(new BlockPos(30, 0, 30));
        positions.add(new BlockPos(2, 0, 0));
        positions.add(new BlockPos(12, 0, 0));

        BlockPos selected = MiningPositionSelection.popNearest(positions, new Vec3(0.5D, 0.5D, 0.5D));

        assertEquals(new BlockPos(2, 0, 0), selected);
        assertFalse(positions.contains(selected));
    }

    @Test
    void returnsNullForEmptyCandidates() {
        assertNull(MiningPositionSelection.popNearest(new Stack<>(), Vec3.ZERO));
    }
}
