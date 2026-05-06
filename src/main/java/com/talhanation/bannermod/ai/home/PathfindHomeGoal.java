package com.talhanation.bannermod.ai.home;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.EnumSet;
import java.util.function.Supplier;

/**
 * HOMEASSIGN-003: Shared "go home at night" AI goal for citizens, workers,
 * and recruits.
 *
 * <p>Triggers when (a) the entity has a non-null home assigned via the
 * HOMEASSIGN-002 API, (b) the level is currently night (or the per-entity
 * {@code lowStaminaSignal} returns true — recruits use that for forced rest),
 * (c) the entity is not in combat. The goal then pathfinds to within 3 blocks
 * of {@code homePos} via the entity's existing {@link PathNavigation} (which
 * is {@code AsyncGroundPathNavigation} on every wired entity), and on arrival
 * either calls {@link LivingEntity#startSleeping(BlockPos)} when the home
 * block is a vacant bed, or simply stops the navigation as an idle/sleep-on
 * -ground fallback.
 *
 * <p>Restart preservation: the goal does not persist any in-flight pathing
 * state. {@link #canUse()} re-evaluates against {@code homePos} (which is
 * persistent via HOMEASSIGN-002) and the world clock every tick budget, so
 * after world reload the entity naturally resumes walking home if conditions
 * still hold.
 *
 * <p>The goal is intentionally generic over {@link PathfinderMob} so a single
 * implementation covers the recruit/worker hierarchy and the standalone
 * {@link com.talhanation.bannermod.entity.citizen.CitizenEntity} which sits on
 * a different inheritance branch.
 */
public final class PathfindHomeGoal extends Goal {

    private final PathfinderMob mob;
    private final Supplier<BlockPos> homePosSupplier;
    private final Supplier<Boolean> lowStaminaSignal;
    private final double speedModifier;
    private long lastCanUseCheck = Long.MIN_VALUE;
    private BlockPos targetHome;

    public PathfindHomeGoal(PathfinderMob mob,
                            Supplier<BlockPos> homePosSupplier,
                            Supplier<Boolean> lowStaminaSignal,
                            double speedModifier) {
        this.mob = mob;
        this.homePosSupplier = homePosSupplier;
        this.lowStaminaSignal = lowStaminaSignal;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public PathfindHomeGoal(PathfinderMob mob, Supplier<BlockPos> homePosSupplier) {
        this(mob, homePosSupplier, () -> Boolean.FALSE, 1.0D);
    }

    @Override
    public boolean canUse() {
        // Throttle: identical cadence to RestGoal (1Hz) so the goal does not
        // hot-loop the homePos lookup every tick. lastCanUseCheck starts at
        // Long.MIN_VALUE so the first call always passes the throttle.
        long now = this.mob.level().getGameTime();
        if (this.lastCanUseCheck != Long.MIN_VALUE && now - this.lastCanUseCheck < 20L) {
            return false;
        }
        this.lastCanUseCheck = now;
        return shouldGoHome();
    }

    @Override
    public boolean canContinueToUse() {
        return shouldGoHome();
    }

    /** Visible for tests: the unthrottled trigger predicate that {@link #canUse()} delegates to. */
    boolean shouldGoHome() {
        BlockPos home = this.homePosSupplier.get();
        if (home == null) {
            return false;
        }
        if (this.mob.getTarget() != null) {
            // Combat takes priority — same predicate as the existing RestGoal.
            return false;
        }
        if (this.mob.isSleeping()) {
            // Already asleep, keep the goal active so it does not yield to
            // strolls. canContinueToUse keeps us latched until daybreak.
            this.targetHome = home;
            return isNightOrLowStamina();
        }
        return isNightOrLowStamina();
    }

    private boolean isNightOrLowStamina() {
        if (this.mob.level().isNight()) {
            return true;
        }
        Boolean low = this.lowStaminaSignal.get();
        return low != null && low.booleanValue();
    }

    @Override
    public void start() {
        super.start();
        this.targetHome = this.homePosSupplier.get();
    }

    @Override
    public void stop() {
        super.stop();
        if (this.mob.isSleeping()) {
            this.mob.stopSleeping();
        }
        this.mob.getNavigation().stop();
        this.targetHome = null;
    }

    @Override
    public void tick() {
        BlockPos home = this.targetHome;
        if (home == null) {
            return;
        }
        if (this.mob.isSleeping()) {
            // Already in bed — stay put. Sleep healing / morale belongs to the
            // existing RestGoal for recruits; we only own pathfinding.
            this.mob.getNavigation().stop();
            return;
        }

        double distSqr = this.mob.position().distanceToSqr(
                home.getX() + 0.5D, home.getY() + 0.5D, home.getZ() + 0.5D);
        // 3 blocks = 9 sqr. Acceptance: "within 3 blocks of homePos".
        if (distSqr <= 9.0D) {
            this.mob.getNavigation().stop();
            this.mob.getLookControl().setLookAt(
                    home.getX() + 0.5D, home.getY() + 1.0D, home.getZ() + 0.5D,
                    10.0F, (float) this.mob.getMaxHeadXRot());
            tryEnterBed(home);
            return;
        }

        // Recompute the path periodically; the entity's navigation already
        // handles incremental updates internally.
        if (this.mob.tickCount % 20 == 0) {
            this.mob.getNavigation().moveTo(
                    home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D, this.speedModifier);
        }
    }

    private void tryEnterBed(BlockPos home) {
        Level level = this.mob.level();
        BlockState state = level.getBlockState(home);
        if (!state.isBed(level, home, this.mob)) {
            return;
        }
        if (state.hasProperty(BlockStateProperties.OCCUPIED)
                && state.getValue(BlockStateProperties.OCCUPIED)) {
            return;
        }
        this.mob.startSleeping(home);
        this.mob.setSleepingPos(home);
    }
}
