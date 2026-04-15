package com.talhanation.bannermod.ai.civilian;

import com.talhanation.bannermod.entity.civilian.workarea.MiningPatternSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MiningPatternPlanner {

    public SegmentPlan planTunnelSegment(MiningPatternSettings settings, BlockPos origin, Direction facing, int segmentIndex) {
        BlockPos anchor = origin.relative(facing, segmentIndex + 1).below((segmentIndex + 1) * settings.descentStep());
        BlockPos standingCell = anchor.relative(facing.getOpposite()).above();
        return buildSegment(settings, anchor, facing, standingCell, floorAnchor(origin, facing, segmentIndex, settings));
    }

    public SegmentPlan planBranchSegment(MiningPatternSettings settings, BlockPos origin, Direction facing, int segmentIndex) {
        int spacing = Math.max(1, settings.branchSpacing());
        BlockPos corridorAnchor = origin.relative(facing, segmentIndex * spacing + 1).offset(0, settings.heightOffset(), 0);
        Direction branchDirection = segmentIndex % 2 == 0 ? facing.getClockWise() : facing.getCounterClockWise();
        BlockPos anchor = corridorAnchor.relative(branchDirection, settings.branchLength());
        BlockPos standingCell = corridorAnchor.above();
        return buildSegment(settings, anchor, facing, standingCell, corridorAnchor.below());
    }

    private SegmentPlan buildSegment(MiningPatternSettings settings, BlockPos anchor, Direction facing, BlockPos blockedCell, BlockPos floorAnchor) {
        Direction lateral = facing.getClockWise();
        int lateralOffset = settings.width() / 2;
        Set<BlockPos> breakTargets = new LinkedHashSet<>();
        Set<BlockPos> floorTargets = new LinkedHashSet<>();
        for (int y = 0; y < settings.height(); y++) {
            for (int x = 0; x < settings.width(); x++) {
                BlockPos candidate = anchor.relative(lateral, x - lateralOffset).above(y);
                if (!candidate.equals(blockedCell.below())) {
                    breakTargets.add(candidate.immutable());
                }
                floorTargets.add(floorAnchor.relative(lateral, x - lateralOffset).immutable());
            }
        }
        return new SegmentPlan(anchor, new ArrayList<>(breakTargets), new ArrayList<>(floorTargets));
    }

    private BlockPos floorAnchor(BlockPos origin, Direction facing, int segmentIndex, MiningPatternSettings settings) {
        int floorSegment = Math.max(0, segmentIndex);
        return origin.relative(facing, floorSegment).below(floorSegment * settings.descentStep() + 1);
    }

    public record SegmentPlan(BlockPos anchor, List<BlockPos> breakTargets, List<BlockPos> floorTargets) {
    }
}
