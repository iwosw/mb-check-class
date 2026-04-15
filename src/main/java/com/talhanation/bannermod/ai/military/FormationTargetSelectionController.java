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
    private static final SelectionState<CohortKey, LivingEntity> RUNTIME_STATE = new SelectionState<>();

    private FormationTargetSelectionController() {
    }

    public static Decision<LivingEntity> beginRuntimeSelection(RuntimeSelectionRequest request, Predicate<LivingEntity> targetValidator) {
        return beginSelection(RUNTIME_STATE, request.selectionRequest(), targetValidator);
    }

    public static @Nullable LivingEntity completeRuntimeSelection(RuntimeSelectionRequest request, @Nullable LivingEntity selectedTarget) {
        return completeSelection(RUNTIME_STATE, request.selectionRequest(), selectedTarget);
    }

    public static void resetProfiling() {
        RUNTIME_STATE.reset();
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
