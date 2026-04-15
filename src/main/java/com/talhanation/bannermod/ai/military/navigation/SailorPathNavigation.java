package com.talhanation.bannermod.ai.military.navigation;

import com.google.common.collect.ImmutableSet;
import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.entity.military.CaptainEntity;
import com.talhanation.bannermod.ai.pathfinding.AsyncPathfinder;
import com.talhanation.bannermod.ai.pathfinding.AsyncWaterBoundPathNavigation;
import com.talhanation.bannermod.ai.pathfinding.NodeEvaluatorGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

public class SailorPathNavigation extends AsyncWaterBoundPathNavigation {
    private static BiFunction<Integer, NodeEvaluator, PathFinder> pathfinderSupplier = (p_26453_, nodeEvaluator) -> new PathFinder(nodeEvaluator, p_26453_);
    CaptainEntity captain;

    private static final NodeEvaluatorGenerator nodeEvaluatorGenerator = SailorNodeEvaluator::new;

    public SailorPathNavigation(CaptainEntity sailor, Level level) {
        super(sailor, level);
        this.captain = sailor;
        if(RecruitsServerConfig.UseAsyncPathfinding.get()) {
            pathfinderSupplier = (p_26453_, nodeEvaluator) -> new AsyncPathfinder(nodeEvaluator, p_26453_, nodeEvaluatorGenerator, this.level);
        }
    }

    protected @NotNull PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new SailorNodeEvaluator();
        return pathfinderSupplier.apply(maxVisitedNodes, this.nodeEvaluator);
    }

    @Override
    protected boolean canUpdatePath() {
        return true;
    }

    @Nullable
    public Path createPath(@NotNull BlockPos blockPos, int additionalOffsetXYZ, boolean targetBlockAbove, int accuracy) {
        return this.createPath(ImmutableSet.of(blockPos), additionalOffsetXYZ, targetBlockAbove, accuracy, (float)this.mob.getAttributeValue(Attributes.FOLLOW_RANGE));
    }
}
