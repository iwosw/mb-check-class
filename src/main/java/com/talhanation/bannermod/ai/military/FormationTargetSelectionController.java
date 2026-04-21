package com.talhanation.bannermod.ai.military;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Predicate;

public final class FormationTargetSelectionController {
    private static final long DEFAULT_MAX_ASSIGNMENT_AGE_TICKS = 40L;
    private static final long DEFAULT_ASSIGNEE_TTL_TICKS = 40L;
    public static final int DEFAULT_MAX_ASSIGNEES_PER_TARGET = 3;

    private static final SelectionState<CohortKey, LivingEntity> RUNTIME_STATE = new SelectionState<>();
    private static final Map<CohortKey, Map<UUID, AssigneeEntry>> ASSIGNEE_COUNTS = new ConcurrentHashMap<>();

    private FormationTargetSelectionController() {
    }

    public static int assigneeCount(CohortKey cohort, LivingEntity target, long gameTime) {
        return assigneeCount(cohort, target, gameTime, DEFAULT_ASSIGNEE_TTL_TICKS);
    }

    public static int assigneeCount(CohortKey cohort, LivingEntity target, long gameTime, long ttlTicks) {
        if (cohort == null || target == null) {
            return 0;
        }
        Map<UUID, AssigneeEntry> counts = ASSIGNEE_COUNTS.get(cohort);
        if (counts == null) {
            return 0;
        }
        AssigneeEntry entry = counts.get(target.getUUID());
        if (entry == null) {
            return 0;
        }
        if (gameTime - entry.lastTick() > ttlTicks) {
            counts.remove(target.getUUID(), entry);
            return 0;
        }
        return entry.count();
    }

    public static void recordAssignee(CohortKey cohort, LivingEntity target, long gameTime) {
        recordAssignee(cohort, target, gameTime, DEFAULT_ASSIGNEE_TTL_TICKS);
    }

    public static void recordAssignee(CohortKey cohort, LivingEntity target, long gameTime, long ttlTicks) {
        if (cohort == null || target == null) {
            return;
        }
        Map<UUID, AssigneeEntry> counts = ASSIGNEE_COUNTS.computeIfAbsent(cohort, k -> new ConcurrentHashMap<>());
        counts.compute(target.getUUID(), (k, existing) -> {
            if (existing == null || gameTime - existing.lastTick() > ttlTicks) {
                return new AssigneeEntry(1, gameTime);
            }
            return new AssigneeEntry(existing.count() + 1, gameTime);
        });
    }

    public static void clearAssigneeCounts() {
        ASSIGNEE_COUNTS.clear();
    }

    private record AssigneeEntry(int count, long lastTick) {
    }

    public static Decision<LivingEntity> beginRuntimeSelection(RuntimeSelectionRequest request, Predicate<LivingEntity> targetValidator) {
        return beginSelection(RUNTIME_STATE, request.selectionRequest(), targetValidator);
    }

    public static @Nullable LivingEntity completeRuntimeSelection(RuntimeSelectionRequest request, @Nullable LivingEntity selectedTarget) {
        return completeSelection(RUNTIME_STATE, request.selectionRequest(), selectedTarget);
    }

    public static void resetProfiling() {
        RUNTIME_STATE.reset();
        ASSIGNEE_COUNTS.clear();
    }

    public static ProfilingSnapshot profilingSnapshot() {
        return RUNTIME_STATE.snapshot();
    }

    public static <K, T> Decision<T> beginSelection(SelectionState<K, T> state, SelectionRequest<K> request, Predicate<T> targetValidator) {
        state.recordRequest();

        if (request.cohortKey() == null) {
            state.recordLocalFallback();
            return Decision.localFallback();
        }

        Assignment<T> existingAssignment = state.assignment(request.cohortKey());
        if (!request.formationEligible()) {
            if (existingAssignment != null) {
                state.invalidate(request.cohortKey());
            }
            state.recordLocalFallback();
            return Decision.localFallback();
        }

        if (existingAssignment == null) {
            return Decision.computeSharedSelection();
        }

        if (existingAssignment.target() == null) {
            if (request.gameTime() == existingAssignment.assignedAtGameTime()) {
                state.recordReuse();
                return Decision.reused(null);
            }
            state.invalidate(request.cohortKey());
            return Decision.computeSharedSelection();
        }

        if (request.gameTime() - existingAssignment.assignedAtGameTime() > request.maxAssignmentAgeTicks()) {
            state.invalidate(request.cohortKey());
            return Decision.computeSharedSelection();
        }

        if (!targetValidator.test(existingAssignment.target())) {
            state.invalidate(request.cohortKey());
            return Decision.computeSharedSelection();
        }

        state.recordReuse();
        return Decision.reused(existingAssignment.target());
    }

    public static <K, T> @Nullable T completeSelection(SelectionState<K, T> state, SelectionRequest<K> request, @Nullable T selectedTarget) {
        state.recordComputation();
        if (request.cohortKey() == null || !request.formationEligible() || selectedTarget == null) {
            if (request.cohortKey() != null && request.formationEligible()) {
                state.assign(request.cohortKey(), null, request.gameTime());
            }
            return selectedTarget;
        }

        state.assign(request.cohortKey(), selectedTarget, request.gameTime());
        return selectedTarget;
    }

    public record CohortKey(UUID ownerId, UUID groupId) {
    }

    public record SelectionRequest<K>(
            @Nullable K cohortKey,
            boolean formationEligible,
            long gameTime,
            long maxAssignmentAgeTicks
    ) {
        public SelectionRequest(@Nullable K cohortKey, boolean formationEligible, long gameTime) {
            this(cohortKey, formationEligible, gameTime, DEFAULT_MAX_ASSIGNMENT_AGE_TICKS);
        }
    }

    public record RuntimeSelectionRequest(
            @Nullable UUID ownerId,
            @Nullable UUID groupId,
            boolean formationEligible,
            long gameTime,
            long maxAssignmentAgeTicks
    ) {
        public RuntimeSelectionRequest(@Nullable UUID ownerId, @Nullable UUID groupId, boolean formationEligible, long gameTime) {
            this(ownerId, groupId, formationEligible, gameTime, DEFAULT_MAX_ASSIGNMENT_AGE_TICKS);
        }

        public SelectionRequest<CohortKey> selectionRequest() {
            if (ownerId == null || groupId == null) {
                return new SelectionRequest<>(null, false, gameTime, maxAssignmentAgeTicks);
            }
            return new SelectionRequest<>(new CohortKey(ownerId, groupId), formationEligible, gameTime, maxAssignmentAgeTicks);
        }
    }

    public record Decision<T>(DecisionType type, @Nullable T target) {
        public static <T> Decision<T> reused(T target) {
            return new Decision<>(DecisionType.REUSED_SHARED_SELECTION, target);
        }

        public static <T> Decision<T> computeSharedSelection() {
            return new Decision<>(DecisionType.COMPUTE_SHARED_SELECTION, null);
        }

        public static <T> Decision<T> localFallback() {
            return new Decision<>(DecisionType.LOCAL_FALLBACK, null);
        }
    }

    public enum DecisionType {
        REUSED_SHARED_SELECTION,
        COMPUTE_SHARED_SELECTION,
        LOCAL_FALLBACK
    }

    public record ProfilingSnapshot(
            long formationSelectionRequests,
            long formationSelectionComputations,
            long formationSelectionAssignments,
            long formationSelectionReuses,
            long formationSelectionInvalidations,
            long localFallbackSearches
    ) {
    }

    private record Assignment<T>(@Nullable T target, long assignedAtGameTime) {
    }

    public static final class SelectionState<K, T> {
        private final Map<K, Assignment<T>> assignments = new ConcurrentHashMap<>();
        private final LongAdder formationSelectionRequests = new LongAdder();
        private final LongAdder formationSelectionComputations = new LongAdder();
        private final LongAdder formationSelectionAssignments = new LongAdder();
        private final LongAdder formationSelectionReuses = new LongAdder();
        private final LongAdder formationSelectionInvalidations = new LongAdder();
        private final LongAdder localFallbackSearches = new LongAdder();

        private Assignment<T> assignment(K cohortKey) {
            return assignments.get(cohortKey);
        }

        private void assign(K cohortKey, T target, long gameTime) {
            assignments.put(cohortKey, new Assignment<>(target, gameTime));
            formationSelectionAssignments.increment();
        }

        private void invalidate(K cohortKey) {
            if (assignments.remove(cohortKey) != null) {
                formationSelectionInvalidations.increment();
            }
        }

        private void recordRequest() {
            formationSelectionRequests.increment();
        }

        private void recordComputation() {
            formationSelectionComputations.increment();
        }

        private void recordReuse() {
            formationSelectionReuses.increment();
        }

        private void recordLocalFallback() {
            localFallbackSearches.increment();
        }

        public void reset() {
            assignments.clear();
            formationSelectionRequests.reset();
            formationSelectionComputations.reset();
            formationSelectionAssignments.reset();
            formationSelectionReuses.reset();
            formationSelectionInvalidations.reset();
            localFallbackSearches.reset();
        }

        public ProfilingSnapshot snapshot() {
            return new ProfilingSnapshot(
                    formationSelectionRequests.sum(),
                    formationSelectionComputations.sum(),
                    formationSelectionAssignments.sum(),
                    formationSelectionReuses.sum(),
                    formationSelectionInvalidations.sum(),
                    localFallbackSearches.sum()
            );
        }
    }
}
