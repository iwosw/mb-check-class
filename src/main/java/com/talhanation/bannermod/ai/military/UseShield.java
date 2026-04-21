package com.talhanation.bannermod.ai.military;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.HorsemanEntity;
import com.talhanation.bannermod.ai.pathfinding.AsyncPathfinderMob;
import com.talhanation.bannermod.util.AttackUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ToolActions;

import javax.annotation.Nullable;

public class UseShield extends Goal {
    /** Radius (blocks) used to auto-raise shield under SHIELD_WALL stance. */
    private static final double SHIELD_WALL_SCAN_RADIUS = 8.0D;
    /** Radius (blocks) used to auto-raise shield under LINE_HOLD stance. */
    private static final double LINE_HOLD_SCAN_RADIUS = 5.0D;
    /** Re-scan interval for hostile proximity (ticks). */
    private static final int HOSTILE_SCAN_INTERVAL_TICKS = 10;

    public final AsyncPathfinderMob entity;

    /** Step 2.C: cached stance-driven auto-block decision. */
    private int nextHostileScanTick = Integer.MIN_VALUE;
    private boolean cachedStanceAutoBlock;
    /** Nearest hostile observed during the last scan, or null. Used by Step 2.D. */
    @Nullable
    private LivingEntity cachedNearestHostile;

    public UseShield(AsyncPathfinderMob recruit){
        this.entity = recruit;
    }

    public boolean canUse() {
        if (entity instanceof AbstractRecruitEntity recruit){
            boolean forced = recruit.getShouldBlock();
            boolean stanceAuto = shouldStanceAutoBlock(recruit);
            boolean normal = canRaiseShield() && !recruit.isFollowing() && recruit.canBlock() && !recruit.getShouldMovePos();

            return (forced || stanceAuto || normal) && hasShieldInHand() && !this.entity.swinging;
        }
        else return canRaiseShield() && hasShieldInHand() && !this.entity.swinging;
    }

    public boolean canContinueToUse() {
        return canUse();
    }

    public void start() {
        if (this.entity.getOffhandItem().getItem().canPerformAction(entity.getOffhandItem(), ToolActions.SHIELD_BLOCK)){
            this.entity.startUsingItem(InteractionHand.OFF_HAND);
            this.entity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.12D);
        }
    }
    public boolean hasShieldInHand(){
        if(entity instanceof AbstractRecruitEntity recruit){
            recruit.switchOffHandItem(itemStack -> itemStack.getItem().canPerformAction(entity.getOffhandItem(), ToolActions.SHIELD_BLOCK));
        }
        return this.entity.getOffhandItem().getItem().canPerformAction(entity.getOffhandItem(), ToolActions.SHIELD_BLOCK);
    }
    public  void stop(){
        this.entity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        entity.stopUsingItem();
    }

    public void tick() {
        if (this.entity.getUsedItemHand() == InteractionHand.OFF_HAND) {
            this.entity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.16D);
        } else {
            this.entity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        }

        // Step 2.D: while holding a shield wall in formation, pivot the body towards
        // the nearest hostile at most 6deg / tick. Head yaw is left alone so the
        // recruit can still glance elsewhere.
        if (entity instanceof AbstractRecruitEntity recruit
                && recruit.isInFormation
                && recruit.getCombatStance() == CombatStance.SHIELD_WALL
                && recruit.isBlocking()) {
            LivingEntity hostile = this.cachedNearestHostile;
            if (hostile != null && hostile.isAlive()) {
                float targetYaw = ShieldBlockGeometry.yawToward(
                        recruit.yBodyRot,
                        recruit.getX(), recruit.getZ(),
                        hostile.getX(), hostile.getZ()
                );
                recruit.yBodyRot = FormationYawPolicy.clampBodyYaw(
                        recruit.yBodyRot, targetYaw, FormationYawPolicy.SHIELD_WALL_YAW_DELTA_LIMIT_DEG);
            }
        }
    }

    /**
     * Step 2.C: auto-raise shield under SHIELD_WALL / LINE_HOLD when a hostile is nearby.
     * Cached for {@link #HOSTILE_SCAN_INTERVAL_TICKS} to avoid per-tick scans.
     */
    private boolean shouldStanceAutoBlock(AbstractRecruitEntity recruit) {
        CombatStance stance = recruit.getCombatStance();
        if (stance != CombatStance.SHIELD_WALL && stance != CombatStance.LINE_HOLD) {
            this.cachedNearestHostile = null;
            return false;
        }
        if (!recruit.canBlock() || recruit.getShouldMovePos()) {
            return false;
        }

        int tick = recruit.tickCount;
        if (tick >= this.nextHostileScanTick) {
            double radius = stance == CombatStance.SHIELD_WALL ? SHIELD_WALL_SCAN_RADIUS : LINE_HOLD_SCAN_RADIUS;
            LivingEntity nearest = findNearestHostile(recruit, radius);
            this.cachedNearestHostile = nearest;
            this.cachedStanceAutoBlock = nearest != null;
            // Stagger per-recruit via UUID hash on the FIRST scan so recruits in one
            // cohort don't all scan on the same tick. Subsequent scans run every 10 ticks.
            int jitter = this.nextHostileScanTick == Integer.MIN_VALUE
                    ? Math.floorMod(recruit.getUUID().hashCode(), HOSTILE_SCAN_INTERVAL_TICKS)
                    : 0;
            this.nextHostileScanTick = tick + HOSTILE_SCAN_INTERVAL_TICKS + jitter;
        }
        return this.cachedStanceAutoBlock;
    }

    @Nullable
    private static LivingEntity findNearestHostile(AbstractRecruitEntity recruit, double radius) {
        LivingEntity best = null;
        double bestDistSqr = Double.POSITIVE_INFINITY;
        for (LivingEntity candidate : recruit.level().getEntitiesOfClass(
                LivingEntity.class, recruit.getBoundingBox().inflate(radius))) {
            if (candidate == recruit || !candidate.isAlive()) continue;
            if (!recruit.targetingConditions.test(recruit, candidate)) continue;
            double d = recruit.distanceToSqr(candidate);
            if (d < bestDistSqr) {
                bestDistSqr = d;
                best = candidate;
            }
        }
        return best;
    }

    public boolean canRaiseShield() {
        boolean isSelfTargeted = false;
        LivingEntity target = this.entity.getTarget();

        if (target != null && target.isAlive()) {

            if (target instanceof Mob mobTarget) {
                isSelfTargeted = mobTarget.getTarget() != null && mobTarget.getTarget().is(entity);
            }
            else if (target instanceof Player player){
                LivingEntity lastHurtMob = player.getLastHurtMob();
                isSelfTargeted = lastHurtMob != null && lastHurtMob.is(entity);
            }

            ItemStack itemStackInHand = target.getItemInHand(InteractionHand.MAIN_HAND);
            double ownReach = AttackUtil.getAttackReachSqr(entity);
            Item itemInHand = itemStackInHand.getItem();
            double distanceToTarget = this.entity.distanceToSqr(target);
            boolean isTargetInReachToBlock = this.entity instanceof HorsemanEntity horseman && horseman.getVehicle() instanceof AbstractHorse ?  70 > distanceToTarget :  120 > distanceToTarget ;

            boolean isDanger = itemInHand instanceof AxeItem || itemInHand instanceof PickaxeItem || itemInHand instanceof SwordItem;

            if(isSelfTargeted){
                //For Ranged
                if(target instanceof RangedAttackMob || (itemInHand instanceof CrossbowItem && CrossbowItem.isCharged(itemStackInHand)) || (itemInHand instanceof BowItem && target.getTicksUsingItem() > 0)){
                    return distanceToTarget > ownReach * 1.5;
                }
                //For Melee
                else return (isDanger || target instanceof Monster) && isTargetInReachToBlock;
            }
        }
        return false;
    }
}