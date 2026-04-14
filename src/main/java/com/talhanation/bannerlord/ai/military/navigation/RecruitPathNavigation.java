package com.talhanation.bannerlord.ai.military.navigation;

import com.talhanation.bannerlord.entity.shared.*;

import com.talhanation.recruits.config.RecruitsServerConfig;
import com.talhanation.bannerlord.entity.shared.AbstractRecruitEntity;
import com.talhanation.bannerlord.ai.pathfinding.AsyncGroundPathNavigation;
import com.talhanation.bannerlord.ai.pathfinding.AsyncPathfinder;
import com.talhanation.bannerlord.ai.pathfinding.GlobalPathfindingController;
import com.talhanation.bannerlord.ai.pathfinding.NodeEvaluatorGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class RecruitPathNavigation extends AsyncGroundPathNavigation {
    private static final double FLOW_FIELD_TARGET_MATCH_DISTANCE = 1.5D;
    private static final double FLOW_FIELD_COHORT_SCAN_RADIUS = 12.0D;
    private static final double FLOW_FIELD_MIN_PATH_DISTANCE_SQR = 16.0D;
    private static final Set<UUID> FLOW_FIELD_TEST_OVERRIDES = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static BiFunction<Integer, NodeEvaluator, PathFinder> pathfinderSupplier = (p_26453_, nodeEvaluator) -> new PathFinder(nodeEvaluator, p_26453_);
    AbstractRecruitEntity recruit;

    private static final NodeEvaluatorGenerator nodeEvaluatorGenerator = () -> {
        NodeEvaluator nodeEvaluator = new RecruitsPathNodeEvaluator();

        nodeEvaluator.setCanOpenDoors(true);
        nodeEvaluator.setCanPassDoors(true);
        nodeEvaluator.setCanFloat(true);

        return nodeEvaluator;
    };

    public RecruitPathNavigation(AbstractRecruitEntity recruit, Level world) {
        super(recruit, world);
        this.recruit = recruit;
        if(RecruitsServerConfig.UseAsyncPathfinding.get()) {
            pathfinderSupplier = (p_26453_, nodeEvaluator) -> new AsyncPathfinder(nodeEvaluator, p_26453_, nodeEvaluatorGenerator, this.level);
        }
    }

    @Override
    protected @NotNull PathFinder createPathFinder(int range) {
        this.nodeEvaluator = new RecruitsPathNodeEvaluator();
        this.nodeEvaluator.setCanOpenDoors(true);
        this.nodeEvaluator.setCanPassDoors(true);
        this.nodeEvaluator.setCanFloat(true);

        return pathfinderSupplier.apply(range, this.nodeEvaluator);
    }

    @Override
    public boolean moveTo(double x, double y, double z, double speed) {
        this.recruit.setMaxFallDistance(1);
        ((RecruitsPathNodeEvaluator) this.nodeEvaluator).setTarget((int) x, (int) y, (int) z);
        BlockPos targetPos = new BlockPos((int) x, (int) y, (int) z);
        net.minecraft.world.level.pathfinder.Path path = this.createPath(targetPos, 0, this.flowFieldPrototypeRequest(targetPos));
        return this.moveTo(path, speed);
    }

    private GlobalPathfindingController.FlowFieldPrototypeRequest flowFieldPrototypeRequest(BlockPos targetPos) {
        int cohortSize = this.countEligibleCohortSize(targetPos);
        return new GlobalPathfindingController.FlowFieldPrototypeRequest(
                this.isFlowFieldPrototypeEnabled(),
                this.isFlowFieldEligible(targetPos, cohortSize),
                cohortSize
        );
    }

    private boolean isFlowFieldPrototypeEnabled() {
        UUID recruitId = this.recruit.getUUID();
        return RecruitsServerConfig.EnableOptionalFlowFieldPrototype.get()
                || (recruitId != null && FLOW_FIELD_TEST_OVERRIDES.contains(recruitId));
    }

    private boolean isFlowFieldEligible(BlockPos targetPos, int cohortSize) {
        if (!this.isFormationLikeMovement(this.recruit) || this.recruit.getTarget() != null) {
            return false;
        }
        if (this.recruit.getGroupUUID().isEmpty() || this.recruit.distanceToSqr(Vec3.atCenterOf(targetPos)) < FLOW_FIELD_MIN_PATH_DISTANCE_SQR) {
            return false;
        }
        return this.matchesStaticDestination(this.recruit, targetPos)
                && cohortSize >= RecruitsServerConfig.FlowFieldPrototypeMinCohortSize.get();
    }

    private int countEligibleCohortSize(BlockPos targetPos) {
        UUID groupId = this.recruit.getGroup();
        UUID ownerId = this.recruit.getOwnerUUID();
        if (groupId == null || ownerId == null) {
            return 1;
        }

        AABB bounds = this.recruit.getBoundingBox().inflate(FLOW_FIELD_COHORT_SCAN_RADIUS);
        return this.level.getEntitiesOfClass(AbstractRecruitEntity.class, bounds, candidate -> candidate.isAlive()
                && candidate != this.recruit
                && Objects.equals(candidate.getOwnerUUID(), ownerId)
                && groupId.equals(candidate.getGroup())
                && this.isFormationLikeMovement(candidate)
                && candidate.getTarget() == null
                && this.matchesStaticDestination(candidate, targetPos)).size() + 1;
    }

    private boolean isFormationLikeMovement(AbstractRecruitEntity candidate) {
        int followState = candidate.getFollowState();
        return candidate.isInFormation || followState == 2 || followState == 3;
    }

    private boolean matchesStaticDestination(AbstractRecruitEntity candidate, BlockPos targetPos) {
        BlockPos movePos = candidate.getMovePos();
        if (movePos != null) {
            return movePos.closerThan(targetPos, FLOW_FIELD_TARGET_MATCH_DISTANCE);
        }

        Vec3 holdPos = candidate.getHoldPos();
        return holdPos != null && holdPos.distanceToSqr(Vec3.atCenterOf(targetPos)) <= FLOW_FIELD_TARGET_MATCH_DISTANCE * FLOW_FIELD_TARGET_MATCH_DISTANCE;
    }

    public static void setFlowFieldPrototypeTestOverride(AbstractRecruitEntity recruit, boolean enabled) {
        UUID recruitId = recruit.getUUID();
        if (recruitId == null) {
            return;
        }
        if (enabled) {
            FLOW_FIELD_TEST_OVERRIDES.add(recruitId);
        } else {
            FLOW_FIELD_TEST_OVERRIDES.remove(recruitId);
        }
    }

    public static void clearFlowFieldPrototypeTestOverrides() {
        FLOW_FIELD_TEST_OVERRIDES.clear();
    }
}
