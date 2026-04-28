package com.talhanation.bannermod.ai.military;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.HorsemanEntity;
import com.talhanation.bannermod.ai.pathfinding.AsyncPathfinderMob;
import com.talhanation.bannermod.util.AttackUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.ItemAbilities;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UseShield extends Goal {
    /** Radius (blocks) used to auto-raise shield under SHIELD_WALL stance. */
    private static final double SHIELD_WALL_SCAN_RADIUS = 8.0D;
    /** Radius (blocks) used to auto-raise shield under LINE_HOLD stance. */
    private static final double LINE_HOLD_SCAN_RADIUS = 5.0D;
    /** Re-scan interval for hostile proximity (ticks). */
    private static final int HOSTILE_SCAN_INTERVAL_TICKS = 10;
    /** Re-scan interval for mounted charge brace checks (ticks). */
    private static final int BRACE_SCAN_INTERVAL_TICKS = 5;
    /** Stage 4.C: attribute-modifier uuid for the transient brace knockback-resistance boost. */
    private static final UUID BRACE_KB_RESIST_UUID = UUID.fromString("b7a1c3d2-4e5f-4c9b-8e2a-0b6c4d1f5a21");
    /** Stage 4.C: extra knockback resistance (ADDITION operand) applied while bracing. */
    private static final double BRACE_KB_RESIST_BONUS = 0.5D;

    public final AsyncPathfinderMob entity;

    /** Step 2.C: cached stance-driven auto-block decision. */
    private int nextHostileScanTick = Integer.MIN_VALUE;
    private boolean cachedStanceAutoBlock;
    /** Nearest hostile observed during the last scan, or null. Used by Step 2.D. */
    @Nullable
    private LivingEntity cachedNearestHostile;
    private int nextBraceScanTick = Integer.MIN_VALUE;
    private boolean cachedBraceForCharge;

    public UseShield(AsyncPathfinderMob recruit){
        this.entity = recruit;
    }

    public boolean canUse() {
        if (entity instanceof AbstractRecruitEntity recruit){
            boolean bracing = shouldBraceForCharge(recruit);
            applyBraceState(recruit, bracing);
            boolean forced = recruit.getShouldBlock();
            boolean stanceAuto = shouldStanceAutoBlock(recruit);
            boolean normal = canRaiseShield() && !recruit.isFollowing() && recruit.canBlock() && !recruit.getShouldMovePos();

            return (bracing || forced || stanceAuto || normal) && hasShieldInHand() && !this.entity.swinging;
        }
        else return canRaiseShield() && hasShieldInHand() && !this.entity.swinging;
    }

    public boolean canContinueToUse() {
        return canUse();
    }

    public void start() {
        if (this.entity.getOffhandItem().getItem().canPerformAction(entity.getOffhandItem(), ItemAbilities.SHIELD_BLOCK)){
            this.entity.startUsingItem(InteractionHand.OFF_HAND);
            this.entity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.12D);
        }
    }
    public boolean hasShieldInHand(){
        if(entity instanceof AbstractRecruitEntity recruit){
            recruit.switchOffHandItem(itemStack -> itemStack.getItem().canPerformAction(entity.getOffhandItem(), ItemAbilities.SHIELD_BLOCK));
        }
        return this.entity.getOffhandItem().getItem().canPerformAction(entity.getOffhandItem(), ItemAbilities.SHIELD_BLOCK);
    }
    public  void stop(){
        this.entity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        entity.stopUsingItem();
        if (entity instanceof AbstractRecruitEntity recruit) {
            applyBraceState(recruit, false);
        }
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

    /**
     * Stage 4.C: brace-for-charge decision. Scans for hostile mounted entities
     * within {@link BraceAgainstChargePolicy#BRACE_RADIUS} and delegates to the
     * pure policy. Non-LOOSE stances only, and recruit must hold a shield or
     * reach weapon.
     */
    private boolean shouldBraceForCharge(AbstractRecruitEntity recruit) {
        CombatStance stance = recruit.getCombatStance();
        if (stance == null || stance == CombatStance.LOOSE) {
            return false;
        }
        boolean hasShield = recruit.canBlock() && hasShieldInHand();
        boolean hasReach = WeaponReach.effectiveReachFor(recruit.getMainHandItem().getItem()) > 0.0D;
        if (!hasShield && !hasReach) {
            this.cachedBraceForCharge = false;
            return false;
        }

        int tick = recruit.tickCount;
        if (tick < this.nextBraceScanTick) {
            return this.cachedBraceForCharge;
        }

        List<BraceAgainstChargePolicy.HostileObservation> hostiles = collectMountedHostiles(recruit);
        this.cachedBraceForCharge = BraceAgainstChargePolicy.shouldBrace(stance, hasShield, hasReach, hostiles);
        int jitter = this.nextBraceScanTick == Integer.MIN_VALUE
                ? Math.floorMod(recruit.getUUID().hashCode(), BRACE_SCAN_INTERVAL_TICKS)
                : 0;
        this.nextBraceScanTick = tick + BRACE_SCAN_INTERVAL_TICKS + jitter;
        return this.cachedBraceForCharge;
    }

    private static List<BraceAgainstChargePolicy.HostileObservation> collectMountedHostiles(AbstractRecruitEntity recruit) {
        List<BraceAgainstChargePolicy.HostileObservation> out = new ArrayList<>();
        for (LivingEntity candidate : recruit.level().getEntitiesOfClass(
                LivingEntity.class,
                recruit.getBoundingBox().inflate(BraceAgainstChargePolicy.BRACE_RADIUS))) {
            if (candidate == recruit || !candidate.isAlive()) continue;
            if (!recruit.targetingConditions.test(recruit, candidate)) continue;
            Entity vehicle = candidate.getVehicle();
            boolean mounted = candidate.isPassenger() && vehicle instanceof LivingEntity;
            if (!mounted) continue;
            double distSqr = recruit.distanceToSqr(candidate);
            // Per the spec, velocity check is optional — treat proximity as sufficient.
            out.add(new BraceAgainstChargePolicy.HostileObservation(distSqr, true, true));
        }
        return out;
    }

    /**
     * Stage 4.C: toggle the recruit's transient brace state. When turning on,
     * also halt navigation, raise the shield, and add a knockback-resistance
     * attribute modifier. When turning off, remove the modifier.
     */
    private void applyBraceState(AbstractRecruitEntity recruit, boolean bracing) {
        if (recruit.isBracing == bracing) {
            return;
        }
        recruit.isBracing = bracing;
        AttributeInstance kbResist = recruit.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbResist != null) {
            AttributeModifier existing = kbResist.getModifier(BRACE_KB_RESIST_UUID);
            if (bracing) {
                if (existing == null) {
                    kbResist.addTransientModifier(new AttributeModifier(
                            BRACE_KB_RESIST_UUID,
                            "bannermod.brace_kb_resist",
                            BRACE_KB_RESIST_BONUS,
                            AttributeModifier.Operation.ADDITION));
                }
            } else if (existing != null) {
                kbResist.removeModifier(BRACE_KB_RESIST_UUID);
            }
        }
        if (bracing) {
            recruit.setShouldBlock(true);
            recruit.getNavigation().stop();
        }
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
