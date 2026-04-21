package com.talhanation.bannermod.entity.military;

import com.talhanation.bannermod.ai.military.CombatStance;
import com.talhanation.bannermod.ai.military.ShieldBlockGeometry;
import com.talhanation.bannermod.ai.military.ShieldMitigation;
import com.talhanation.bannermod.events.RecruitEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.raid.Raider;

final class RecruitCombatOverrideService {

    /** Ticks of shield stagger applied after a successful directional block (HYW parity ≈ 5s). */
    private static final int BLOCK_STAGGER_COOLDOWN_TICKS = 100;
    /** Extra mitigation multiplier for shieldman-class recruits (stacked onto stance). */
    private static final float SHIELDMAN_BONUS_REMAINING = 0.9f;
    /** Knockback strength applied to attackers whose melee blow is blocked at the front. */
    private static final float BLOCK_KNOCKBACK_STRENGTH = 0.5f;

    private RecruitCombatOverrideService() {
    }

    static float prepareIncomingDamage(AbstractRecruitEntity recruit, DamageSource damageSource, float damage) {
        Entity attacker = damageSource.getEntity();
        if (attacker != null && !(attacker instanceof Player) && !(attacker instanceof AbstractArrow)) {
            damage = (damage + 1.0F) / 2.0F;
        }

        if (recruit.getMorale() > 0) recruit.setMoral(recruit.getMorale() - 0.25F);

        damage = applyShieldMitigation(recruit, damageSource, damage);

        if (recruit.isBlocking()) recruit.hurtCurrentlyUsedShield(damage);

        if (attacker instanceof LivingEntity living && RecruitEvents.canAttack(recruit, living)) {
            propagateProtectTarget(recruit, living);
            recruit.assignReactiveCombatTarget(living);

            if (recruit.getShouldProtect() && recruit.getProtectingMob() instanceof AbstractRecruitEntity patrolLeader) {
                patrolLeader.assignReactiveCombatTarget(living);
            }
        }

        return damage;
    }

    /**
     * Step 2.A/B: directional shield mitigation with stance-driven reduction and
     * HYW-style stagger cooldown on a successful block.
     */
    private static float applyShieldMitigation(AbstractRecruitEntity recruit, DamageSource damageSource, float damage) {
        if (damage <= 0f) {
            return damage;
        }
        if (!recruit.isBlocking() && !recruit.getShouldBlock()) {
            return damage;
        }
        if (!isBlockableDamageSource(damageSource)) {
            return damage;
        }
        double[] origin = attackerOrigin(recruit, damageSource);
        if (origin == null) {
            return damage;
        }
        boolean inCone = ShieldBlockGeometry.isInFrontCone(
                recruit.yBodyRot,
                recruit.getX(), recruit.getZ(),
                origin[0], origin[1],
                ShieldBlockGeometry.FRONT_CONE_HALF_DEG
        );
        if (!inCone) {
            return damage;
        }

        CombatStance stance = recruit.getCombatStance();
        boolean staggered = recruit.blockCoolDown > 0;
        float mitigated = ShieldMitigation.damageAfterBlock(stance, damage, true, true, staggered);
        if (recruit instanceof RecruitShieldmanEntity) {
            mitigated *= SHIELDMAN_BONUS_REMAINING;
        }

        // Step 2.B: bump stagger cooldown on successful block. Take max so an existing
        // higher cooldown (e.g. from a disabled shield) is not reduced.
        recruit.blockCoolDown = Math.max(recruit.blockCoolDown, BLOCK_STAGGER_COOLDOWN_TICKS);

        // Step 2.E: light knockback on melee attackers whose blow was blocked.
        Entity direct = damageSource.getDirectEntity();
        if (direct instanceof LivingEntity livingDirect && !(direct instanceof AbstractArrow)) {
            float yawRad = recruit.yBodyRot * ((float) Math.PI / 180F);
            livingDirect.knockback(BLOCK_KNOCKBACK_STRENGTH, Mth.sin(yawRad), -Mth.cos(yawRad));
        }

        return mitigated;
    }

    private static boolean isBlockableDamageSource(DamageSource damageSource) {
        if (damageSource == null) {
            return false;
        }
        if (damageSource.is(DamageTypeTags.BYPASSES_SHIELD)) {
            return false;
        }
        if (damageSource.is(DamageTypeTags.IS_FALL)
                || damageSource.is(DamageTypeTags.IS_DROWNING)
                || damageSource.is(DamageTypeTags.IS_FIRE)
                || damageSource.is(DamageTypeTags.IS_FREEZING)
                || damageSource.is(DamageTypes.IN_WALL)
                || damageSource.is(DamageTypes.STARVE)
                || damageSource.is(DamageTypes.MAGIC)
                || damageSource.is(DamageTypes.WITHER)
                || damageSource.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            return false;
        }
        if (damageSource.is(DamageTypeTags.IS_PROJECTILE)) {
            return true;
        }
        Entity direct = damageSource.getDirectEntity();
        Entity attacker = damageSource.getEntity();
        return direct != null && attacker instanceof LivingEntity;
    }

    private static double[] attackerOrigin(AbstractRecruitEntity recruit, DamageSource damageSource) {
        Entity direct = damageSource.getDirectEntity();
        if (direct != null && direct != recruit) {
            return new double[]{direct.getX(), direct.getZ()};
        }
        Entity attacker = damageSource.getEntity();
        if (attacker != null && attacker != recruit) {
            return new double[]{attacker.getX(), attacker.getZ()};
        }
        return null;
    }

    static boolean handleKillRewards(AbstractRecruitEntity recruit, LivingEntity victim) {
        recruit.addXp(5);
        recruit.setKills(recruit.getKills() + 1);
        if (recruit.getMorale() < 100) recruit.setMoral(recruit.getMorale() + 1);

        if (victim instanceof Player) {
            recruit.addXp(45);
            if (recruit.getMorale() < 100) recruit.setMoral(recruit.getMorale() + 9);
        }

        if (victim instanceof Raider) {
            recruit.addXp(5);
            if (recruit.getMorale() < 100) recruit.setMoral(recruit.getMorale() + 2);
        }

        if (victim instanceof Villager villager) {
            if (villager.isBaby()) {
                if (recruit.getMorale() > 0) recruit.setMoral(recruit.getMorale() - 10);
            }
            else if (recruit.getMorale() > 0) {
                recruit.setMoral(recruit.getMorale() - 2);
            }
        }

        if (victim instanceof WitherBoss) {
            recruit.addXp(99);
            if (recruit.getMorale() < 100) recruit.setMoral(recruit.getMorale() + 9);
        }

        if (victim instanceof IronGolem) {
            recruit.addXp(49);
            if (recruit.getMorale() > 0) recruit.setMoral(recruit.getMorale() - 1);
        }

        if (victim instanceof EnderDragon) {
            recruit.addXp(999);
            if (recruit.getMorale() < 100) recruit.setMoral(recruit.getMorale() + 49);
        }

        recruit.checkLevel();
        return true;
    }

    private static void propagateProtectTarget(AbstractRecruitEntity recruit, LivingEntity attacker) {
        if (recruit.getFollowState() != 5) {
            return;
        }

        for (AbstractRecruitEntity nearbyRecruit : recruit.getCommandSenderWorld().getEntitiesOfClass(
                AbstractRecruitEntity.class,
                recruit.getBoundingBox().inflate(32D)
        )) {
            if (nearbyRecruit.getUUID().equals(nearbyRecruit.getProtectUUID()) && nearbyRecruit.isAlive() && !nearbyRecruit.equals(attacker)) {
                nearbyRecruit.assignReactiveCombatTarget(attacker);
            }
        }
    }
}
