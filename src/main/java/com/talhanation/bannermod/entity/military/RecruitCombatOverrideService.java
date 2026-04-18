package com.talhanation.bannermod.entity.military;

import com.talhanation.bannermod.events.RecruitEvents;
import net.minecraft.world.damagesource.DamageSource;
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
    private RecruitCombatOverrideService() {
    }

    static float prepareIncomingDamage(AbstractRecruitEntity recruit, DamageSource damageSource, float damage) {
        Entity attacker = damageSource.getEntity();
        if (attacker != null && !(attacker instanceof Player) && !(attacker instanceof AbstractArrow)) {
            damage = (damage + 1.0F) / 2.0F;
        }

        if (recruit.getMorale() > 0) recruit.setMoral(recruit.getMorale() - 0.25F);
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
