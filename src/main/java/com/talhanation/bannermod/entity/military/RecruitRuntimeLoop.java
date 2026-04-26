package com.talhanation.bannermod.entity.military;

import com.talhanation.bannermod.ai.military.FormationTargetSelectionController;
import com.talhanation.bannermod.ai.military.RecruitAiLodPolicy;
import com.talhanation.bannermod.ai.military.async.AsyncManager;
import com.talhanation.bannermod.ai.military.async.AsyncTaskWithCallback;
import com.talhanation.bannermod.ai.military.controller.RecruitCommandStateTransitions;
import com.talhanation.bannermod.compat.BetterCombatAttackBridge;
import com.talhanation.bannermod.config.RecruitsServerConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

final class RecruitRuntimeLoop {
    /** Ticks after a target kill during which LOD is forced to FULL + fast base gate. */
    private static final int RECENT_TARGET_LOSS_WINDOW_TICKS = 60;
    /** Accelerated base gate (ticks) used inside the recent-loss window. */
    private static final int RECENT_TARGET_LOSS_BASE_GATE_TICKS = 5;
    /** Round-robin threshold: skip an already-contested shared target above this count. */
    private static final int SHARED_TARGET_PILEON_THRESHOLD = 3;

    private static final TargetSearchProfilingCounters TARGET_SEARCH_PROFILING = new TargetSearchProfilingCounters();

    private RecruitRuntimeLoop() {
    }

    static void aiStep(AbstractRecruitEntity recruit) {
        recruit.needsColorUpdate = false;
        if (recruit instanceof IRangedRecruit && (recruit.tickCount + recruit.targetSearchTickOffset()) % 20 == 0) {
            recruit.pickUpArrows();
        }
        if (recruit.needsTeamUpdate) {
            recruit.updateTeam();
        }
        if (recruit.needsGroupUpdate) {
            recruit.updateGroup();
        }
    }

    static void tick(AbstractRecruitEntity recruit) {
        if (recruit.despawnTimer > 0) {
            recruit.despawnTimer--;
        }
        if (recruit.despawnTimer == 0) {
            recruitCheckDespawn(recruit);
        }

        tickPayment(recruit);

        if (recruit.getMountTimer() > 0) {
            recruit.setMountTimer(recruit.getMountTimer() - 1);
        }
        if (recruit.getUpkeepTimer() > 0) {
            recruit.setUpkeepTimer(recruit.getUpkeepTimer() - 1);
        }
        if (recruit.getHunger() >= 70F && recruit.getHealth() < recruit.getMaxHealth()) {
            recruit.heal(1.0F / 50F);
        }

        updateMoveArrivalState(recruit);

        if (recruit.attackCooldown > 0) {
            recruit.attackCooldown--;
        }
        BetterCombatAttackBridge.tickPendingAttack(recruit);

        runTargetSearchTick(recruit);

        LivingEntity currentTarget = recruit.getTarget();
        if (currentTarget != null && (currentTarget.isDeadOrDying() || currentTarget.isRemoved())) {
            recruit.setTarget(null);
            recruit.lastTargetLossTick = recruit.tickCount;
        }

        if (recruit.rotateTicks > 0 && recruit.getNavigation().isDone()) {
            recruit.setYRot(recruit.ownerRot);
            recruit.yRotO = recruit.ownerRot;
            recruit.rotateTicks--;
        }
    }

    static void searchForTargets(AbstractRecruitEntity recruit) {
        if (!(recruit.getCommandSenderWorld() instanceof ServerLevel serverLevel)) {
            return;
        }

        TARGET_SEARCH_PROFILING.recordSearch();
        if (RecruitsServerConfig.UseAsyncTargetFinding.get()) {
            searchForTargetsAsync(recruit, serverLevel);
        } else {
            searchForTargetsSync(recruit, serverLevel);
        }
    }

    static void resetTargetSearchProfiling() {
        TARGET_SEARCH_PROFILING.reset();
    }

    static AbstractRecruitEntity.TargetSearchProfilingSnapshot targetSearchProfilingSnapshot() {
        return TARGET_SEARCH_PROFILING.snapshot();
    }

    private static void tickPayment(AbstractRecruitEntity recruit) {
        if (!RecruitsServerConfig.RecruitsPayment.get()) {
            return;
        }
        if (recruit.paymentTimer > 0) {
            recruit.paymentTimer--;
        }
        if (recruit.paymentTimer == 0) {
            if (recruit.getUpkeepPos() != null || recruit.getUpkeepUUID() != null) {
                recruit.forcedUpkeep = true;
            } else {
                recruit.checkPayment(recruit.getInventory());
            }
        }
    }

    private static void updateMoveArrivalState(AbstractRecruitEntity recruit) {
        int resolvedFollowState = RecruitCommandStateTransitions.afterMoveArrival(recruit.reachedMovePos, recruit.getFollowState());
        if (!recruit.reachedMovePos) {
            return;
        }
        recruit.setFollowState(resolvedFollowState);
        recruit.reachedMovePos = false;
    }

    private static void runTargetSearchTick(AbstractRecruitEntity recruit) {
        if (!recruit.isAlive() || recruit.getState() == 3) {
            return;
        }

        RecruitAiLodPolicy.Evaluation targetSearchLod = evaluateTargetSearchLod(recruit);
        TARGET_SEARCH_PROFILING.recordLodTier(targetSearchLod.tier());

        if (!isBaseTargetSearchTick(recruit)) {
            return;
        }

        TARGET_SEARCH_PROFILING.recordSearchOpportunity();
        if (targetSearchLod.shouldRunSearch()) {
            recruit.searchForTargets();
        } else {
            TARGET_SEARCH_PROFILING.recordLodSkip();
        }
    }

    private static boolean isBaseTargetSearchTick(AbstractRecruitEntity recruit) {
        int gate = isWithinRecentTargetLossWindow(recruit)
                ? RECENT_TARGET_LOSS_BASE_GATE_TICKS
                : RecruitAiLodPolicy.DEFAULT_FULL_SEARCH_INTERVAL;
        return (recruit.tickCount + recruit.targetSearchTickOffset()) % gate == 0;
    }

    private static boolean isWithinRecentTargetLossWindow(AbstractRecruitEntity recruit) {
        int last = recruit.lastTargetLossTick;
        if (last == Integer.MIN_VALUE) {
            return false;
        }
        int elapsed = recruit.tickCount - last;
        return elapsed >= 0 && elapsed <= RECENT_TARGET_LOSS_WINDOW_TICKS;
    }

    private static RecruitAiLodPolicy.Evaluation evaluateTargetSearchLod(AbstractRecruitEntity recruit) {
        RecruitAiLodPolicy.Settings settings = RecruitAiLodPolicy.settingsFromConfig();
        LivingEntity currentTarget = recruit.getTarget();
        boolean hasLiveTarget = currentTarget != null && currentTarget.isAlive() && !currentTarget.isRemoved();
        double liveTargetDistanceSqr = hasLiveTarget ? recruit.distanceToSqr(currentTarget) : Double.POSITIVE_INFINITY;
        double maxRelevantDistance = settings.reducedDistance();
        Player nearbyPlayer = maxRelevantDistance > 0 ? recruit.level().getNearestPlayer(recruit, maxRelevantDistance) : null;
        double nearestPlayerDistanceSqr = nearbyPlayer != null ? recruit.distanceToSqr(nearbyPlayer) : Double.POSITIVE_INFINITY;

        return RecruitAiLodPolicy.evaluate(new RecruitAiLodPolicy.Context(
                recruit.hurtTime > 0,
                hasLiveTarget,
                liveTargetDistanceSqr,
                nearestPlayerDistanceSqr,
                recruit.tickCount,
                recruit.targetSearchTickOffset(),
                isWithinRecentTargetLossWindow(recruit)
        ), settings);
    }

    private static void searchForTargetsAsync(AbstractRecruitEntity recruit, ServerLevel serverLevel) {
        FormationTargetSelectionController.RuntimeSelectionRequest selectionRequest = createFormationSelectionRequest(recruit);
        FormationTargetSelectionController.Decision<LivingEntity> selectionDecision = FormationTargetSelectionController.beginRuntimeSelection(
                selectionRequest,
                target -> isValidSharedTarget(recruit, target)
        );
        FormationTargetSelectionController.CohortKey cohortKey = resolveCohortKey(recruit);
        long gameTime = recruit.level().getGameTime();

        if (selectionDecision.type() == FormationTargetSelectionController.DecisionType.REUSED_SHARED_SELECTION) {
            LivingEntity shared = selectionDecision.target();
            if (shared != null && !isSharedTargetOverContested(cohortKey, shared, gameTime)) {
                TARGET_SEARCH_PROFILING.recordTargetAssigned();
                RecruitCombatTargeting.applyCombatTarget(recruit, shared);
                recordAssignee(cohortKey, shared, gameTime);
                return;
            }
            if (shared == null) {
                RecruitCombatTargeting.applyCombatTarget(recruit, null);
                return;
            }
            // Shared target is pile-on saturated: fall through to local round-robin pick.
        }

        AbstractRecruitEntity.NearbyCombatCandidates scan = recruit.scanNearbyCombatCandidates(serverLevel, 40D);
        List<LivingEntity> nearby = scan.candidates();
        TARGET_SEARCH_PROFILING.recordAsyncSearch(scan.observedCount());

        ToIntFunction<LivingEntity> scorer = assigneeScorerFor(cohortKey, gameTime);

        if (selectionDecision.type() == FormationTargetSelectionController.DecisionType.COMPUTE_SHARED_SELECTION) {
            List<LivingEntity> candidates = recruit.filterCombatCandidates(nearby, target -> isValidSharedTarget(recruit, target), true);
            LivingEntity picked = RecruitCombatTargeting.resolveCombatTargetWithAssigneeSpread(
                    recruit, candidates, candidate -> isValidSharedTarget(recruit, candidate), scorer);
            LivingEntity target = FormationTargetSelectionController.completeRuntimeSelection(selectionRequest, picked);
            finalizeTargetAssignment(recruit, target, cohortKey, gameTime);
            return;
        }

        // LOCAL_FALLBACK or saturated-shared fallback.
        Supplier<List<LivingEntity>> findTargetsTask = () -> recruit.filterCombatCandidates(nearby, target -> isValidSharedTarget(recruit, target), true);
        Consumer<List<LivingEntity>> handleTargets = targets -> {
            if (!recruit.isAlive() || recruit.isRemoved()) {
                return;
            }
            LivingEntity target = RecruitCombatTargeting.resolveCombatTargetWithAssigneeSpread(
                    recruit, targets, candidate -> isValidSharedTarget(recruit, candidate), scorer);
            finalizeTargetAssignment(recruit, target, cohortKey, gameTime);
        };

        AsyncManager.executor.execute(new AsyncTaskWithCallback<>(findTargetsTask, handleTargets, serverLevel));
    }

    private static void searchForTargetsSync(AbstractRecruitEntity recruit, ServerLevel serverLevel) {
        FormationTargetSelectionController.RuntimeSelectionRequest selectionRequest = createFormationSelectionRequest(recruit);
        FormationTargetSelectionController.Decision<LivingEntity> selectionDecision = FormationTargetSelectionController.beginRuntimeSelection(
                selectionRequest,
                target -> isValidSharedTarget(recruit, target)
        );
        FormationTargetSelectionController.CohortKey cohortKey = resolveCohortKey(recruit);
        long gameTime = recruit.level().getGameTime();

        if (selectionDecision.type() == FormationTargetSelectionController.DecisionType.REUSED_SHARED_SELECTION) {
            LivingEntity shared = selectionDecision.target();
            if (shared != null && !isSharedTargetOverContested(cohortKey, shared, gameTime)) {
                TARGET_SEARCH_PROFILING.recordTargetAssigned();
                RecruitCombatTargeting.applyCombatTarget(recruit, shared);
                recordAssignee(cohortKey, shared, gameTime);
                return;
            }
            if (shared == null) {
                RecruitCombatTargeting.applyCombatTarget(recruit, null);
                return;
            }
            // Shared target is pile-on saturated: fall through to local round-robin pick.
        }

        AbstractRecruitEntity.NearbyCombatCandidates scan = recruit.scanNearbyCombatCandidates(serverLevel, 40D);
        TARGET_SEARCH_PROFILING.recordSyncSearch(scan.observedCount());

        List<LivingEntity> nearby = recruit.filterCombatCandidates(scan.candidates(), target -> isValidSharedTarget(recruit, target), true);
        ToIntFunction<LivingEntity> scorer = assigneeScorerFor(cohortKey, gameTime);
        LivingEntity target = RecruitCombatTargeting.resolveCombatTargetWithAssigneeSpread(
                recruit, nearby, candidate -> isValidSharedTarget(recruit, candidate), scorer);
        if (selectionDecision.type() == FormationTargetSelectionController.DecisionType.COMPUTE_SHARED_SELECTION) {
            target = FormationTargetSelectionController.completeRuntimeSelection(selectionRequest, target);
        }
        finalizeTargetAssignment(recruit, target, cohortKey, gameTime);
    }

    private static FormationTargetSelectionController.CohortKey resolveCohortKey(AbstractRecruitEntity recruit) {
        UUID ownerId = recruit.getOwnerUUID();
        UUID groupId = recruit.getGroup();
        if (ownerId == null || groupId == null) {
            return null;
        }
        return new FormationTargetSelectionController.CohortKey(ownerId, groupId);
    }

    private static boolean isSharedTargetOverContested(FormationTargetSelectionController.CohortKey cohortKey,
                                                       LivingEntity shared,
                                                       long gameTime) {
        if (cohortKey == null || shared == null) {
            return false;
        }
        return FormationTargetSelectionController.assigneeCount(cohortKey, shared, gameTime) >= SHARED_TARGET_PILEON_THRESHOLD;
    }

    private static ToIntFunction<LivingEntity> assigneeScorerFor(FormationTargetSelectionController.CohortKey cohortKey,
                                                                 long gameTime) {
        if (cohortKey == null) {
            return null;
        }
        return candidate -> FormationTargetSelectionController.assigneeCount(cohortKey, candidate, gameTime);
    }

    private static void recordAssignee(FormationTargetSelectionController.CohortKey cohortKey,
                                       LivingEntity target,
                                       long gameTime) {
        if (cohortKey == null || target == null) {
            return;
        }
        FormationTargetSelectionController.recordAssignee(cohortKey, target, gameTime);
    }

    private static void finalizeTargetAssignment(AbstractRecruitEntity recruit,
                                                 @Nullable LivingEntity target,
                                                 FormationTargetSelectionController.CohortKey cohortKey,
                                                 long gameTime) {
        if (target != null) {
            TARGET_SEARCH_PROFILING.recordTargetAssigned();
        }
        RecruitCombatTargeting.applyCombatTarget(recruit, target);
        if (target != null) {
            recordAssignee(cohortKey, target, gameTime);
        }
    }

    private static FormationTargetSelectionController.RuntimeSelectionRequest createFormationSelectionRequest(AbstractRecruitEntity recruit) {
        RecruitCombatTargeting.clearInvalidTargetForSelection(recruit, target -> isValidSharedTarget(recruit, target));
        UUID ownerId = recruit.getOwnerUUID();
        UUID groupId = recruit.getGroup();
        return new FormationTargetSelectionController.RuntimeSelectionRequest(
                ownerId,
                groupId,
                isEligibleForFormationTargetSelection(recruit, ownerId, groupId),
                recruit.level().getGameTime()
        );
    }

    private static boolean isEligibleForFormationTargetSelection(AbstractRecruitEntity recruit, @Nullable UUID ownerId, @Nullable UUID groupId) {
        if (ownerId == null || groupId == null) {
            return false;
        }
        if (recruit.getState() != 1 || !recruit.isAlive()) {
            return false;
        }
        return recruit.isInFormation || recruit.getFollowState() == 2 || recruit.getFollowState() == 3 || recruit.getFollowState() == 5;
    }

    private static boolean isValidSharedTarget(AbstractRecruitEntity recruit, @Nullable LivingEntity target) {
        return target != null
                && target.isAlive()
                && !target.isRemoved()
                // Avoid vanilla forCombat neutral-mob gating (e.g. spider day-passive state).
                // Recruit-specific policy already lives in shouldAttack/canAttack.
                && recruit.shouldAttack(target);
    }

    private static void recruitCheckDespawn(AbstractRecruitEntity recruit) {
        if (recruit.isOwned()) {
            return;
        }

        Entity entity = recruit.getCommandSenderWorld().getNearestPlayer(recruit, -1.0D);
        if (entity == null) {
            return;
        }

        double distanceToPlayerSqr = entity.distanceToSqr(recruit);
        int noDespawnDistance = recruit.getType().getCategory().getNoDespawnDistance();
        int noDespawnDistanceSqr = noDespawnDistance * noDespawnDistance;
        if (recruit.getRandom().nextInt(800) == 0 && distanceToPlayerSqr > (double) noDespawnDistanceSqr) {
            if (recruit.getVehicle() instanceof LivingEntity livingMount) {
                livingMount.discard();
            }
            recruit.discard();
        }
    }

    private static final class TargetSearchProfilingCounters {
        private final LongAdder searchOpportunities = new LongAdder();
        private final LongAdder totalSearches = new LongAdder();
        private final LongAdder asyncSearches = new LongAdder();
        private final LongAdder syncSearches = new LongAdder();
        private final LongAdder candidateEntitiesObserved = new LongAdder();
        private final LongAdder targetsAssigned = new LongAdder();
        private final LongAdder lodSkippedSearches = new LongAdder();
        private final LongAdder lodFullTierTicks = new LongAdder();
        private final LongAdder lodReducedTierTicks = new LongAdder();
        private final LongAdder lodShedTierTicks = new LongAdder();

        private void recordSearchOpportunity() {
            this.searchOpportunities.increment();
        }

        private void recordSearch() {
            this.totalSearches.increment();
        }

        private void recordAsyncSearch(int candidates) {
            this.asyncSearches.increment();
            this.candidateEntitiesObserved.add(candidates);
        }

        private void recordSyncSearch(int candidates) {
            this.syncSearches.increment();
            this.candidateEntitiesObserved.add(candidates);
        }

        private void recordTargetAssigned() {
            this.targetsAssigned.increment();
        }

        private void recordLodSkip() {
            this.lodSkippedSearches.increment();
        }

        private void recordLodTier(RecruitAiLodPolicy.LodTier tier) {
            switch (tier) {
                case FULL -> this.lodFullTierTicks.increment();
                case REDUCED -> this.lodReducedTierTicks.increment();
                case SHED -> this.lodShedTierTicks.increment();
            }
        }

        private void reset() {
            this.searchOpportunities.reset();
            this.totalSearches.reset();
            this.asyncSearches.reset();
            this.syncSearches.reset();
            this.candidateEntitiesObserved.reset();
            this.targetsAssigned.reset();
            this.lodSkippedSearches.reset();
            this.lodFullTierTicks.reset();
            this.lodReducedTierTicks.reset();
            this.lodShedTierTicks.reset();
        }

        private AbstractRecruitEntity.TargetSearchProfilingSnapshot snapshot() {
            return new AbstractRecruitEntity.TargetSearchProfilingSnapshot(
                    this.searchOpportunities.sum(),
                    this.totalSearches.sum(),
                    this.asyncSearches.sum(),
                    this.syncSearches.sum(),
                    this.candidateEntitiesObserved.sum(),
                    this.targetsAssigned.sum(),
                    this.lodSkippedSearches.sum(),
                    this.lodFullTierTicks.sum(),
                    this.lodReducedTierTicks.sum(),
                    this.lodShedTierTicks.sum()
            );
        }
    }
}
