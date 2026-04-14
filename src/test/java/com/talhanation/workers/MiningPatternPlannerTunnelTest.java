package com.talhanation.workers;

import com.talhanation.bannerlord.ai.civilian.MiningPatternPlanner;
import com.talhanation.bannerlord.entity.civilian.workarea.MiningPatternSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MiningPatternPlannerTunnelTest {

    @Test
    void diagonalTunnelAdvancesForwardAndDownForAllFacings() {
        MiningPatternPlanner planner = new MiningPatternPlanner();
        MiningPatternSettings settings = MiningPatternSettings.tunnel(3, 3, 0, true, 1);
        BlockPos origin = new BlockPos(10, 50, 10);

        assertEquals(new BlockPos(10, 48, 8), planner.planTunnelSegment(settings, origin, Direction.NORTH, 1).anchor());
        assertEquals(new BlockPos(10, 48, 12), planner.planTunnelSegment(settings, origin, Direction.SOUTH, 1).anchor());
        assertEquals(new BlockPos(12, 48, 10), planner.planTunnelSegment(settings, origin, Direction.EAST, 1).anchor());
        assertEquals(new BlockPos(8, 48, 10), planner.planTunnelSegment(settings, origin, Direction.WEST, 1).anchor());
    }

    @Test
    void tunnelPreservesCrossSectionAndAvoidsSelfUndermine() {
        MiningPatternPlanner planner = new MiningPatternPlanner();
        MiningPatternSettings settings = MiningPatternSettings.tunnel(3, 3, 0, true, 1);

        MiningPatternPlanner.SegmentPlan segment = planner.planTunnelSegment(settings, new BlockPos(0, 64, 0), Direction.NORTH, 0);

        assertEquals(9, segment.breakTargets().size());
        assertFalse(segment.breakTargets().contains(new BlockPos(0, 63, 0)));
    }

    @Test
    void tunnelEmitsSeparateFloorCloseTargets() {
        MiningPatternPlanner planner = new MiningPatternPlanner();
        MiningPatternSettings settings = MiningPatternSettings.tunnel(5, 3, 0, true, 2);

        MiningPatternPlanner.SegmentPlan segment = planner.planTunnelSegment(settings, new BlockPos(0, 64, 0), Direction.EAST, 1);

        assertEquals(5, segment.floorTargets().size());
        assertTrue(segment.floorTargets().stream().allMatch(pos -> pos.getY() == 61));
    }
}
