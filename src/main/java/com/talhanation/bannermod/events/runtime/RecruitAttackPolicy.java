package com.talhanation.bannermod.events.runtime;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.MessengerEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

final class RecruitAttackPolicy {
    private RecruitAttackPolicy() {
    }

    static boolean canAttack(LivingEntity attacker, LivingEntity target) {
        if (target == null || !target.isAlive()) return false;
        if (target instanceof Player player) return canAttackPlayer(attacker, player);
        if (target instanceof AbstractRecruitEntity targetRecruit) return canAttackRecruit(attacker, targetRecruit);
        if (target instanceof Animal animal) return canAttackAnimal(attacker, animal);
        return RecruitDiplomacyPolicy.canHarmTeam(attacker, target);
    }

    static boolean canAttackAnimal(LivingEntity attacker, Animal animal) {
        if (attacker instanceof AbstractRecruitEntity recruit) {
            if (recruit.getVehicle() != null && recruit.getVehicle().getUUID().equals(animal.getUUID())) return false;
            if (recruit.getProtectUUID() != null && recruit.getProtectUUID().equals(recruit.getProtectUUID())) return false;
            if (animal.isVehicle()) {
                if (animal.getFirstPassenger() instanceof AbstractRecruitEntity targetRecruit) return canAttackRecruit(attacker, targetRecruit);
                if (animal.getFirstPassenger() instanceof Player playerTarget) return canAttackPlayer(attacker, playerTarget);
            }
        }
        return RecruitDiplomacyPolicy.canHarmTeam(attacker, animal);
    }

    static boolean canAttackPlayer(LivingEntity attacker, Player player) {
        if (attacker instanceof AbstractRecruitEntity recruit) {
            if (player.getUUID().equals(recruit.getOwnerUUID()) || player.getUUID().equals(recruit.getProtectUUID()) || player.isCreative() || player.isSpectator()) {
                return false;
            }
        }
        return RecruitDiplomacyPolicy.canHarmTeam(attacker, player);
    }

    static boolean canAttackRecruit(LivingEntity attacker, AbstractRecruitEntity targetRecruit) {
        if (attacker.equals(targetRecruit)) return false;
        if (attacker instanceof AbstractRecruitEntity attackerRecruit) {
            if (attackerRecruit.isOwned() && targetRecruit.isOwned() && attackerRecruit.getOwnerUUID().equals(targetRecruit.getOwnerUUID())) return false;
            if (attackerRecruit.getTeam() != null && targetRecruit.getTeam() != null && attackerRecruit.getTeam().equals(targetRecruit.getTeam()) && !attackerRecruit.getTeam().isAllowFriendlyFire()) return false;
            if (attackerRecruit.getProtectUUID() != null && attackerRecruit.getProtectUUID().equals(targetRecruit.getProtectUUID())) return false;
            if (attackerRecruit.getGroup() != null && attackerRecruit.getGroup().equals(targetRecruit.getGroup())) return false;
            if (targetRecruit instanceof MessengerEntity messenger && messenger.isAtMission()) return false;
        }
        return RecruitDiplomacyPolicy.canHarmTeam(attacker, targetRecruit);
    }
}
