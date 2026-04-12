package com.talhanation.workers;

import com.talhanation.workers.entities.ai.MiningPatternPlanner;
import com.talhanation.workers.entities.workarea.MiningPatternSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MiningPatternPlannerBranchTest {

    @Test
    void branchMiningKeepsMainCorridorHeightAndAlternatesSides() {
        MiningPatternPlanner planner = new MiningPatternPlanner();
        MiningPatternSettings settings = MiningPatternSettings.branch(3, 3, -12, true, 4, 6);
        BlockPos origin = new BlockPos(0, 60, 0);

        MiningPatternPlanner.SegmentPlan left = planner.planBranchSegment(settings, origin, Direction.NORTH, 0);
        MiningPatternPlanner.SegmentPlan right = planner.planBranchSegment(settings, origin, Direction.NORTH, 1);

        assertEquals(48, left.anchor().getY());
        assertEquals(48, right.anchor().getY());
        assertEquals(6, Math.abs(left.anchor().getX()));
        assertEquals(-left.anchor().getX(), right.anchor().getX());
    }

    @Test
    void branchMiningStopsAtConfiguredLengthAndAvoidsSelfUndermine() {
        MiningPatternPlanner planner = new MiningPatternPlanner();
        MiningPatternSettings settings = MiningPatternSettings.branch(3, 3, 0, true, 3, 5);

        MiningPatternPlanner.SegmentPlan segment = planner.planBranchSegment(settings, new BlockPos(0, 64, 0), Direction.SOUTH, 0);

        assertEquals(-5, segment.anchor().getX());
        assertFalse(segment.breakTargets().contains(new BlockPos(0, 63, 1)));
    }
}
