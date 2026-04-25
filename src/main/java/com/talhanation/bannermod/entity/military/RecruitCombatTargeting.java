package com.talhanation.bannermod.entity.military;

import com.talhanation.bannermod.config.RecruitsServerConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

final class RecruitCombatTargeting {
    private static final int TARGET_STICKINESS_RANK_WINDOW = 3;
    private static final double TARGET_STICKINESS_DISTANCE_BUFFER_SQR = 36.0D;
    /** Per-extra-assignee scoring penalty (squared-distance equivalent, ~6 blocks² per assignee). */
    private static final double ASSIGNEE_SCORE_PENALTY_SQR = 36.0D;
    /**
     * If a formation has exactly one live target and it is this close, allow
     * local focus fire (multiple recruits can hit the same threat).
     */
    private static final double FORMATION_CLOSE_FOCUS_DISTANCE_SQR_FALLBACK = 16.0D;
    /** Closer-switch hysteresis: new reactive target must be meaningfully closer than current. */
    private static final double REACTIVE_SWITCH_DISTANCE_MARGIN_SQR = 9.0D;

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
        return resolveCombatTargetWithAssigneeSpread(recruit, targets, validityCheck, null);
    }

    /**
     * Pick a target with round-robin spread. When {@code assigneeScorer} is non-null, each
     * candidate is scored as {@code distanceSqr + assignees * ASSIGNEE_SCORE_PENALTY_SQR};
     * the lowest-scoring candidate wins. Tiebreak prefers the current target via stickiness.
     * Passing {@code null} preserves legacy closest-first behaviour.
     */
    static @Nullable LivingEntity resolveCombatTargetWithAssigneeSpread(
            AbstractRecruitEntity recruit,
            List<LivingEntity> targets,
            Predicate<LivingEntity> validityCheck,
            @Nullable ToIntFunction<LivingEntity> assigneeScorer) {
        List<LivingEntity> liveTargets = targets.stream().filter(validityCheck).toList();
        if (liveTargets.isEmpty()) {
            return null;
        }
        LivingEntity currentTarget = recruit.getTarget();
        if (shouldRetainCurrentTarget(recruit, currentTarget, liveTargets, validityCheck)) {
            return currentTarget;
        }
        if (assigneeScorer == null) {
            return liveTargets.get(0);
        }

        LivingEntity best = null;
        double bestScore = Double.POSITIVE_INFINITY;
        boolean allowCloseFocus =
                recruit.isInFormation
                        && liveTargets.size() == 1;
        for (LivingEntity candidate : liveTargets) {
            double distSqr = recruit.distanceToSqr(candidate);
            int assignees = Math.max(0, assigneeScorer.applyAsInt(candidate));
            double assigneePenalty = (double) assignees * ASSIGNEE_SCORE_PENALTY_SQR;
            if (allowCloseFocus && distSqr <= formationCloseFocusDistanceSqr()) {
                assigneePenalty = 0.0D;
            }
            double score = distSqr + assigneePenalty;
            if (score < bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best != null ? best : liveTargets.get(0);
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
        double currentDistSqr = recruit.distanceToSqr(currentTarget);
        double newDistSqr = recruit.distanceToSqr(target);
        // Require the new threat to be meaningfully closer to avoid thrashing between
        // equidistant attackers each tick; the margin is ~3-block radius.
        if (newDistSqr + REACTIVE_SWITCH_DISTANCE_MARGIN_SQR < currentDistSqr) {
            applyCombatTarget(recruit, target);
            return true;
        }
        // Fallback: if the attacker reached us but our current target is out of melee reach,
        // prefer the attacker we can actually hit back.
        double meleeReachSqr = 9.0D;
        if (newDistSqr <= meleeReachSqr && currentDistSqr > meleeReachSqr) {
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

    private static double formationCloseFocusDistanceSqr() {
        try {
            double configured = RecruitsServerConfig.FormationCloseFocusDistance.get();
            if (configured < 0.0D) {
                return FORMATION_CLOSE_FOCUS_DISTANCE_SQR_FALLBACK;
            }
            return configured * configured;
        } catch (IllegalStateException e) {
            return FORMATION_CLOSE_FOCUS_DISTANCE_SQR_FALLBACK;
        }
    }
}
