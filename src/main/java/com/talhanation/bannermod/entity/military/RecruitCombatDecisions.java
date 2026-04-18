package com.talhanation.bannermod.entity.military;

import com.talhanation.bannermod.events.RecruitEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

final class RecruitCombatDecisions {
    private RecruitCombatDecisions() {
    }

    static boolean shouldAttack(AbstractRecruitEntity recruit, LivingEntity target) {
        return switch (recruit.getState()) {
            case 3 -> false;
            case 0 -> shouldAttackOnNeutral(recruit, target) && recruit.canAttack(target);
            case 1 -> (shouldAttackOnNeutral(recruit, target) || shouldAttackOnAggressive(recruit, target)) && recruit.canAttack(target);
            case 2 -> !RecruitEvents.isAlly(recruit.getTeam(), target.getTeam()) && recruit.canAttack(target);
            default -> recruit.canAttack(target);
        };
    }

    static boolean shouldAttackOnNeutral(AbstractRecruitEntity recruit, LivingEntity target) {
        if (isMonster(target) || isAttackingOwnerOrSelf(recruit, target)) return true;
        if (target instanceof Villager) return false;
        return RecruitEvents.isEnemy(recruit.getTeam(), target.getTeam());
    }

    static boolean shouldAttackOnAggressive(AbstractRecruitEntity recruit, LivingEntity target) {
        if (target instanceof Villager) return false;
        return (target instanceof AbstractRecruitEntity || target instanceof Player) && (RecruitEvents.isNeutral(recruit.getTeam(), target.getTeam()) || RecruitEvents.isEnemy(recruit.getTeam(), target.getTeam()));
    }

    static boolean doHurtTarget(AbstractRecruitEntity recruit, @NotNull Entity entity) {
        float damage = (float) recruit.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        if (entity instanceof LivingEntity livingEntity) {
            damage += net.minecraft.world.item.enchantment.EnchantmentHelper.getDamageBonus(recruit.getMainHandItem(), livingEntity.getMobType());
        }
        int fireAspect = net.minecraft.world.item.enchantment.EnchantmentHelper.getFireAspect(recruit);
        if (fireAspect > 0) entity.setSecondsOnFire(fireAspect * 4);
        boolean flag = entity.hurt(recruit.damageSources().mobAttack(recruit), damage);
        if (flag) {
            recruit.doEnchantDamageEffects(recruit, entity);
            recruit.setLastHurtMob(entity);
        }
        recruit.addXp(1);
        if (recruit.getHunger() > 0) recruit.setHunger(recruit.getHunger() - 0.1F);
        recruit.checkLevel();
        if (recruit.getMorale() < 100) recruit.setMoral(recruit.getMorale() + 0.25F);
        recruit.damageMainHandItem();
        return true;
    }

    static boolean isMonster(LivingEntity target) {
        return target instanceof Enemy;
    }

    static boolean isAttackingOwnerOrSelf(AbstractRecruitEntity recruit, LivingEntity target) {
        return target.getLastHurtByMob() != null && (target.getLastHurtByMob().equals(recruit) || target.getLastHurtByMob().equals(recruit.getOwner()));
    }

}
