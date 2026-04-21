package com.talhanation.bannermod.ai.military;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.BowmanEntity;
import com.talhanation.bannermod.entity.military.CrossBowmanEntity;
import com.talhanation.bannermod.util.AttackUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class RecruitMeleeAttackGoal extends Goal {
    protected final AbstractRecruitEntity recruit;
    private final double speedModifier;
    private Path path;
    private int pathingCooldown;
    private final double range;

    public RecruitMeleeAttackGoal(AbstractRecruitEntity recruit, double speedModifier, double range) {
        this.recruit = recruit;
        this.speedModifier = speedModifier;

        this.range = range;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public boolean canUse() {
        LivingEntity target = this.recruit.getTarget();

        if (target == null || !target.isAlive()) {
            return false;
        }

        boolean isClose = target.distanceToSqr(this.recruit) <= range * range;
        boolean canSee = hasReachLineOfSight(target);
        if (isClose && canSee && canAttackHoldPos() && recruit.getState() != 3 && !recruit.needsToGetFood() && !recruit.getShouldMount() && !recruit.getShouldMovePos()) {
            double distance = this.recruit.distanceToSqr(target.getX(), target.getY(), target.getZ());
            this.path = this.recruit.getNavigation().createPath(target, 0);
            if (this.path != null) {
                return true;
            } else {
                double reach = AttackUtil.getAttackReachSqr(recruit);
                return (reach >= distance) && canAttackHoldPos();
            }
        }

        return false;
    }

    public boolean canContinueToUse() {
        LivingEntity target = this.recruit.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        } else if (!hasReachLineOfSight(target)) {
            return false;
        } else {
            boolean canAttackHoldPos = canAttackHoldPos();
            boolean needsToGetFood = recruit.needsToGetFood();
            boolean getShouldMount = recruit.getShouldMount();
            boolean getShouldMovePos = recruit.getShouldMovePos();
            // Step 1.B: break engagement if we've drifted off the stance leash while chasing.
            if (hasDriftedOffFormationLeash()) {
                return false;
            }
            return (!(target instanceof Player) || !target.isSpectator() && !((Player) target).isCreative()) && canAttackHoldPos && recruit.getState() != 3 && !needsToGetFood && !getShouldMount && !getShouldMovePos;
        }
    }

    public void start() {
        this.recruit.setAggressive(true);
        this.pathingCooldown = 0;

        this.recruit.switchMainHandItem(itemStack -> itemStack.getItem() instanceof SwordItem || itemStack.getItem() instanceof AxeItem);
    }

    public void stop() {
        LivingEntity target = this.recruit.getTarget();
        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target)) {
            this.recruit.setTarget(null);
        }

        if(recruit.getShouldRanged()){
            if(this.recruit instanceof CrossBowmanEntity) this.recruit.switchMainHandItem(itemStack -> itemStack.getItem() instanceof CrossbowItem);
            if(this.recruit instanceof BowmanEntity) this.recruit.switchMainHandItem(itemStack -> itemStack.getItem() instanceof BowItem);
        }

        this.recruit.setAggressive(false);
        if (!recruit.isFollowing()) this.recruit.getNavigation().stop();
    }

    public void tick() {
        if (this.pathingCooldown > 0) this.pathingCooldown--;

        if (recruit.horizontalCollision || recruit.minorHorizontalCollision) {
            this.recruit.getJumpControl().jump();
        }

        LivingEntity target = this.recruit.getTarget();
        if (target != null && target.isAlive()) {
            this.recruit.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distanceToTarget = this.recruit.distanceToSqr(target.getX(), target.getY(), target.getZ());
            double reach = AttackUtil.getAttackReachSqr(recruit);

            boolean canSee = hasReachLineOfSight(target);
            boolean isNotFollowing = !recruit.isFollowing();
            boolean coolDownElapsed = this.pathingCooldown <= 0;

            if (distanceToTarget <= reach && canSee) {
                if (isNotFollowing) this.recruit.getNavigation().stop();
                AttackUtil.performAttack(this.recruit, target);
            } else if (canSee && isNotFollowing && coolDownElapsed) {
                this.pathingCooldown = 4 + this.recruit.getRandom().nextInt(4);

                if (distanceToTarget > 2024.0D) {
                    this.pathingCooldown += 10;
                } else if (distanceToTarget > 256.0D) {
                    this.pathingCooldown += 5;
                }
                this.recruit.getNavigation().moveTo(target, this.speedModifier);
            }
        }
    }

    private boolean canAttackHoldPos() {
        Vec3 pos = recruit.getHoldPos();
        LivingEntity target = this.recruit.getTarget();
        if (target != null && pos != null && recruit.getShouldHoldPos()) {
            double distanceToPos = target.distanceToSqr(pos);
            return CombatLeashPolicy.canEngage(
                    distanceToPos,
                    true,
                    recruit.isInFormation,
                    recruit.getCombatStance()
            );
        }
        return true;
    }

    private boolean hasDriftedOffFormationLeash() {
        Vec3 pos = recruit.getHoldPos();
        if (pos == null || !recruit.getShouldHoldPos()) {
            return false;
        }
        double recruitDistSqr = recruit.distanceToSqr(pos);
        return CombatLeashPolicy.hasDriftedOffLeash(
                recruitDistSqr,
                true,
                recruit.isInFormation,
                recruit.getCombatStance()
        );
    }

    /**
     * Stage 3.B: friendly-allowed line of sight for reach weapons.
     *
     * <p>Plain melee still uses vanilla LOS. Spear / pike / sarissa holders
     * (effective reach >= 1) also accept an LOS where the only things between
     * attacker and target are allied recruits — rank-2 spearmen can poke
     * through rank 1 without breaking slot.
     */
    private boolean hasReachLineOfSight(LivingEntity target) {
        if (this.recruit.getSensing().hasLineOfSight(target)) {
            return true;
        }
        ItemStack held = this.recruit.getMainHandItem();
        if (held == null || held.isEmpty()) {
            return false;
        }
        double extraReach = WeaponReach.effectiveReachFor(held.getItem());
        if (extraReach < 1.0D) {
            return false;
        }
        return FriendlyLineOfSight.canReachThroughAllies(
                () -> probeWorld(target),
                this::isAllyEntity
        );
    }

    private FriendlyLineOfSight.SightProbe<Entity> probeWorld(LivingEntity target) {
        Vec3 start = this.recruit.getEyePosition();
        Vec3 end = new Vec3(target.getX(), target.getEyeY(), target.getZ());

        BlockHitResult blockHit = this.recruit.level().clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.recruit));
        boolean blockedByWorld = blockHit != null && blockHit.getType() != HitResult.Type.MISS;

        // Collect intervening entities along the attacker->target segment. Filter
        // the attacker and target themselves; sort nearest-first so the first
        // non-ally short-circuits canReachThroughAllies().
        AABB box = this.recruit.getBoundingBox()
                .expandTowards(end.subtract(start))
                .inflate(1.0D);
        List<Entity> candidates = this.recruit.level().getEntities(this.recruit, box,
                e -> e != target && e.isAlive());
        List<Entity> intersecting = new ArrayList<>();
        for (Entity e : candidates) {
            if (e.getBoundingBox().inflate(0.3D).clip(start, end).isPresent()) {
                intersecting.add(e);
            }
        }
        intersecting.sort(Comparator.comparingDouble(
                e -> e.getBoundingBox().getCenter().distanceToSqr(start)));
        return new FriendlyLineOfSight.SightProbe<>(blockedByWorld, intersecting);
    }

    private boolean isAllyEntity(Entity other) {
        if (!(other instanceof AbstractRecruitEntity ally)) {
            // Unknown entities are NOT friendly — we don't stab through sheep,
            // NPCs, or unrelated mobs.
            return false;
        }
        if (ally == this.recruit) {
            return true;
        }
        // Same team / same owner block intent counts as "friendly" for spear poking.
        if (ally.getTeam() != null && this.recruit.getTeam() != null
                && ally.getTeam().equals(this.recruit.getTeam())) {
            return true;
        }
        // Fall back to the recruit's shouldAttack predicate: if we would NOT
        // attack it, it's safe to poke through.
        return !this.recruit.targetingConditions.test(this.recruit, ally);
    }
}
