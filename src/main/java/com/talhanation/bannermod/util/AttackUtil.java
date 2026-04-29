package com.talhanation.bannermod.util;

import com.talhanation.bannermod.ai.military.AttackCadence;
import com.talhanation.bannermod.ai.military.WeaponReach;
import com.talhanation.bannermod.compat.BetterCombatAttackBridge;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.OptionalInt;
import java.util.Random;
import java.util.concurrent.atomic.LongAdder;

public abstract class AttackUtil {
    private static final MeleeAttackCounters MELEE_ATTACK_COUNTERS = new MeleeAttackCounters();

    public static void checkAndPerformAttack(double distanceSqrToTarget, double reach, AbstractRecruitEntity recruit, LivingEntity target){
        if(distanceSqrToTarget <= reach){
            performAttack(recruit, target);
        }
    }

    public static boolean performAttack(AbstractRecruitEntity recruit, LivingEntity target) {
        MELEE_ATTACK_COUNTERS.attempts.increment();
        if (recruit == null || target == null || !target.isAlive() || target.isRemoved()) {
            MELEE_ATTACK_COUNTERS.denied.increment();
            return false;
        }
        if (!recruit.shouldAttack(target) || recruit.distanceToSqr(target) > getAttackReachSqr(recruit)) {
            MELEE_ATTACK_COUNTERS.denied.increment();
            return false;
        }
        if(recruit.attackCooldown == 0 && !recruit.swinging && recruit.getLookControl().isLookingAtTarget()){
            int bannerCooldown = getAttackCooldown(recruit);
            OptionalInt betterCombatCooldown = BetterCombatAttackBridge.tryStartAttack(recruit, target, bannerCooldown);
            if (betterCombatCooldown.isPresent()) {
                recruit.attackCooldown = Math.max(bannerCooldown, betterCombatCooldown.getAsInt());
                return true;
            }
            boolean damaged;
            if(canPerformHorseAttack(recruit, target)){
                damaged = target.getVehicle() != null && recruit.doHurtTarget(target.getVehicle());
            }
            else damaged = recruit.doHurtTarget(target);
            if (damaged) {
                MELEE_ATTACK_COUNTERS.hits.increment();
            }

            recruit.swing(InteractionHand.MAIN_HAND);
            // Stage 3.D: cadence is baseline + per-weapon windup. We bump cooldown
            // AFTER the hit rather than scheduling a deferred damage tick (see
            // AttackCadence javadoc for the why).
            recruit.attackCooldown = bannerCooldown;
            return damaged;
        }
        MELEE_ATTACK_COUNTERS.cooldownBlocked.increment();
        return false;
    }

    public static MeleeAttackProfilingSnapshot meleeAttackProfilingSnapshot() {
        return MELEE_ATTACK_COUNTERS.snapshot();
    }

    public record MeleeAttackProfilingSnapshot(long attempts, long denied, long cooldownBlocked, long hits) {
    }

    public static boolean canPerformHorseAttack(AbstractRecruitEntity recruit, LivingEntity target) {
        Random random = new Random();
        if(target.getVehicle() instanceof Animal){
            int level = recruit.getXpLevel();
            int chance = Math.min(level*2, 100);

            return random.nextInt(0, 100) <= chance;
        }
        else
            return false;
    }

    public static int getAttackCooldown(AbstractRecruitEntity recruit) {
        double attackSpeed = recruit.getAttributeValue(Attributes.ATTACK_SPEED);

        int base = (int) Math.round(20/attackSpeed);
        int baseline = base + 7;

        // Stage 3.D: per-weapon cadence. Plain sword/axe return the baseline
        // unchanged; spear/pike/sarissa stretch the recovery window so polearms
        // have a real rhythm rather than strictly-superior reach.
        ItemStack held = recruit.getMainHandItem();
        Item item = held == null ? null : held.getItem();
        String id = itemIdOf(item);
        return AttackCadence.cooldownTicksFor(baseline, id);
    }

    private static String itemIdOf(Item item) {
        if (item == null) {
            return null;
        }
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        return key == null ? item.getDescriptionId() : key.toString();
    }

    private static final class MeleeAttackCounters {
        private final LongAdder attempts = new LongAdder();
        private final LongAdder denied = new LongAdder();
        private final LongAdder cooldownBlocked = new LongAdder();
        private final LongAdder hits = new LongAdder();

        private MeleeAttackProfilingSnapshot snapshot() {
            return new MeleeAttackProfilingSnapshot(attempts.sum(), denied.sum(), cooldownBlocked.sum(), hits.sum());
        }
    }

    /*
    Cooldown Infos MC-Wiki
    Swords: 0.6s
    Stone and Wood axe: 1.25s
    Gold/Dia/Neatherite: 1s
    Iron: 1.1s

    1s = 20ticks

    cooldown should be + 5 ticks for gameplay
     */
    public static double getAttackReachSqr(LivingEntity living) {
        float base = 5F;
        // Stage 3.A: per-item reach tag. Spears +1.0b, pikes +2.0b, sarissa +2.5b
        // stack ADDITIVELY with Forge ENTITY_REACH / Epic Fight contributions so
        // polearms gain reach even when the holder has no reach attribute mods.
        double extraReach = 0.0D;
        if (living != null) {
            ItemStack held = living.getMainHandItem();
            if (held != null && !held.isEmpty()) {
                extraReach = WeaponReach.effectiveReachFor(held);
            }
        }
        double effectiveBase = base + extraReach;

        if(living != null && living.getAttribute(Attributes.ENTITY_INTERACTION_RANGE) != null){
            double attackReach = living.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
            //Vanilla reach is 10
            //Epic fight mod:
            // reach +1.0 == 18
            // reach +2.0 == 45
            // reach +3.0 == 60
            if(attackReach > 0){
                // Keep the original (base * 2 * attackReach) shape so Epic Fight
                // numbers stay where modders expect them, then fold in the
                // weapon-tag reach additively on top.
                return 2 * base * attackReach + extraReach * extraReach + 2 * base * extraReach;
            }
        }
        return effectiveBase * effectiveBase;
    }
}
