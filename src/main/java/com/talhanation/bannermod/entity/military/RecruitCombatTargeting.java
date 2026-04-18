package com.talhanation.bannermod.entity.military;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

final class RecruitCombatTargeting {
    private static final int TARGET_STICKINESS_RANK_WINDOW = 3;
    private static final double TARGET_STICKINESS_DISTANCE_BUFFER_SQR = 36.0D;

    private RecruitCombatTargeting() {
    }

    static void clearInvalidTargetForSelection(AbstractRecruitEntity recruit, Predicate<LivingEntity> validityCheck) {
        LivingEntity currentTarget = recruit.getTarget();
        if (currentTarget != null && !validityCheck.test(currentTarget)) {
            recruit.setTarget(null);
        }
    }

    static AbstractRecruitEntity.NearbyCombatCandidates scanNearbyCombatCandidates(AbstractRecruitEntity recruit, ServerLevel serverLevel, double radius) {
        AABB searchBox = recruit.getBoundingBox().inflate(radius);
        List<LivingEntity> nearby = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity != recruit
        );
        return new AbstractRecruitEntity.NearbyCombatCandidates(nearby.size(), nearby);
    }

    static List<LivingEntity> filterCombatCandidates(AbstractRecruitEntity recruit, List<LivingEntity> candidates, Predicate<LivingEntity> filter, boolean sortByDistance) {
        List<LivingEntity> filtered = new ArrayList<>(candidates);
        filtered.removeIf(filter.negate());
        if (sortByDistance) {
            filtered.sort(Comparator.comparingDouble(recruit::distanceToSqr));
        }
        return filtered;
    }

    static @Nullable LivingEntity resolveCombatTargetFromCandidates(AbstractRecruitEntity recruit, List<LivingEntity> targets, Predicate<LivingEntity> validityCheck) {
        List<LivingEntity> liveTargets = targets.stream().filter(validityCheck).toList();
        if (liveTargets.isEmpty()) {
            return null;
        }
        LivingEntity currentTarget = recruit.getTarget();
        if (shouldRetainCurrentTarget(recruit, currentTarget, liveTargets, validityCheck)) {
            return currentTarget;
        }
        return liveTargets.get(0);
    }

    static boolean canAssignCombatTarget(AbstractRecruitEntity recruit, @Nullable LivingEntity target) {
        return target != null && target.isAlive() && !target.isRemoved() && recruit.canAttack(target);
    }

    static boolean assignOrderedCombatTarget(AbstractRecruitEntity recruit, @Nullable LivingEntity target) {
        if (!canAssignCombatTarget(recruit, target)) {
            return false;
        }
        applyCombatTarget(recruit, target);
        return true;
    }

    static boolean assignReactiveCombatTarget(AbstractRecruitEntity recruit, @Nullable LivingEntity target) {
        if (!canAssignCombatTarget(recruit, target)) {
            return false;
        }
        LivingEntity currentTarget = recruit.getTarget();
        if (!canAssignCombatTarget(recruit, currentTarget)) {
            applyCombatTarget(recruit, target);
            return true;
        }
        if (currentTarget == target) {
            return true;
        }
        if (recruit.distanceToSqr(target) <= recruit.distanceToSqr(currentTarget)) {
            applyCombatTarget(recruit, target);
            return true;
        }
        return false;
    }

    static void applyCombatTarget(AbstractRecruitEntity recruit, @Nullable LivingEntity target) {
        if (recruit.getTarget() == target) {
            return;
        }
        recruit.setTarget(target);
    }

    private static boolean shouldRetainCurrentTarget(AbstractRecruitEntity recruit, @Nullable LivingEntity currentTarget, List<LivingEntity> candidates, Predicate<LivingEntity> validityCheck) {
        if (currentTarget == null || !validityCheck.test(currentTarget) || candidates.isEmpty() || !candidates.contains(currentTarget)) {
            return false;
        }
        int currentIndex = candidates.indexOf(currentTarget);
        if (currentIndex <= 0) {
            return true;
        }
        if (currentIndex < TARGET_STICKINESS_RANK_WINDOW) {
            return true;
        }
        double currentDistanceSqr = recruit.distanceToSqr(currentTarget);
        double bestDistanceSqr = recruit.distanceToSqr(candidates.get(0));
        return currentDistanceSqr <= bestDistanceSqr + TARGET_STICKINESS_DISTANCE_BUFFER_SQR;
    }
}
