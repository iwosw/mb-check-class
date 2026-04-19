package com.talhanation.bannermod.ai.military;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class FormationTargetSelectionControllerTest {
    private FormationTargetSelectionController.SelectionState<FormationTargetSelectionController.CohortKey, TestTarget> state;
    private FormationTargetSelectionController.CohortKey cohort;

    @BeforeEach
    void setUp() {
        state = new FormationTargetSelectionController.SelectionState<>();
        cohort = new FormationTargetSelectionController.CohortKey(
                UUID.fromString("00000000-0000-0000-0000-000000000411"),
                UUID.fromString("00000000-0000-0000-0000-000000000412")
        );
    }

    @Test
    void sameFormationCohortReusesPreviouslySelectedLivingTarget() {
        FormationTargetSelectionController.SelectionRequest<FormationTargetSelectionController.CohortKey> firstRequest = request(cohort, true, 20);
        TestTarget sharedTarget = new TestTarget("west-enemy", true);

        FormationTargetSelectionController.Decision<TestTarget> firstDecision = FormationTargetSelectionController.beginSelection(state, firstRequest, TestTarget::alive);
        assertEquals(FormationTargetSelectionController.DecisionType.COMPUTE_SHARED_SELECTION, firstDecision.type());

        FormationTargetSelectionController.completeSelection(state, firstRequest, sharedTarget);

        FormationTargetSelectionController.Decision<TestTarget> secondDecision = FormationTargetSelectionController.beginSelection(
                state,
                request(cohort, true, 20),
                TestTarget::alive
        );

        FormationTargetSelectionController.ProfilingSnapshot snapshot = state.snapshot();

        assertEquals(FormationTargetSelectionController.DecisionType.REUSED_SHARED_SELECTION, secondDecision.type());
        assertSame(sharedTarget, secondDecision.target());
        assertEquals(2, snapshot.formationSelectionRequests());
        assertEquals(1, snapshot.formationSelectionComputations());
        assertEquals(1, snapshot.formationSelectionAssignments());
        assertEquals(1, snapshot.formationSelectionReuses());
        assertEquals(0, snapshot.formationSelectionInvalidations());
        assertEquals(0, snapshot.localFallbackSearches());
    }

    @Test
    void invalidOrIneligibleAssignmentsAreDroppedBeforeFreshSelectionOrFallback() {
        FormationTargetSelectionController.SelectionRequest<FormationTargetSelectionController.CohortKey> request = request(cohort, true, 20);
        TestTarget staleTarget = new TestTarget("stale-enemy", true);
        FormationTargetSelectionController.completeSelection(state, request, staleTarget);
        staleTarget.alive = false;

        FormationTargetSelectionController.Decision<TestTarget> invalidatedDecision = FormationTargetSelectionController.beginSelection(state, request(cohort, true, 25), TestTarget::alive);
        FormationTargetSelectionController.completeSelection(state, request(cohort, true, 25), new TestTarget("fresh-enemy", true));
        FormationTargetSelectionController.Decision<TestTarget> fallbackDecision = FormationTargetSelectionController.beginSelection(state, request(cohort, false, 26), TestTarget::alive);
        FormationTargetSelectionController.completeSelection(state, request(cohort, true, 30), new TestTarget("newer-enemy", true));

        FormationTargetSelectionController.Decision<TestTarget> staleByAgeDecision = FormationTargetSelectionController.beginSelection(
                state,
                new FormationTargetSelectionController.SelectionRequest<>(cohort, true, 80, 10),
                TestTarget::alive
        );

        FormationTargetSelectionController.ProfilingSnapshot snapshot = state.snapshot();

        assertEquals(FormationTargetSelectionController.DecisionType.COMPUTE_SHARED_SELECTION, invalidatedDecision.type());
        assertEquals(FormationTargetSelectionController.DecisionType.LOCAL_FALLBACK, fallbackDecision.type());
        assertNull(fallbackDecision.target());
        assertEquals(FormationTargetSelectionController.DecisionType.COMPUTE_SHARED_SELECTION, staleByAgeDecision.type());
        assertEquals(3, snapshot.formationSelectionInvalidations());
        assertEquals(1, snapshot.localFallbackSearches());
    }

    @Test
    void profilingSeparatelyCountsSharedReuseInvalidationAndFallbackActivity() {
        TestTarget assignedTarget = new TestTarget("enemy", true);
        FormationTargetSelectionController.SelectionRequest<FormationTargetSelectionController.CohortKey> sharedRequest = request(cohort, true, 10);

        FormationTargetSelectionController.beginSelection(state, sharedRequest, TestTarget::alive);
        FormationTargetSelectionController.completeSelection(state, sharedRequest, assignedTarget);
        FormationTargetSelectionController.beginSelection(state, request(cohort, true, 10), TestTarget::alive);
        assignedTarget.alive = false;
        FormationTargetSelectionController.beginSelection(state, request(cohort, true, 11), TestTarget::alive);
        FormationTargetSelectionController.beginSelection(
                state,
                request(new FormationTargetSelectionController.CohortKey(
                        UUID.fromString("00000000-0000-0000-0000-000000000413"),
                        UUID.fromString("00000000-0000-0000-0000-000000000414")
                ), false, 11),
                TestTarget::alive
        );

        FormationTargetSelectionController.ProfilingSnapshot snapshot = state.snapshot();

        assertEquals(4, snapshot.formationSelectionRequests());
        assertEquals(1, snapshot.formationSelectionComputations());
        assertEquals(1, snapshot.formationSelectionAssignments());
        assertEquals(1, snapshot.formationSelectionReuses());
        assertEquals(1, snapshot.formationSelectionInvalidations());
        assertEquals(1, snapshot.localFallbackSearches());
    }

    @Test
    void sameTickNullResolutionIsReusedBeforeTheCohortComputesAgain() {
        FormationTargetSelectionController.SelectionRequest<FormationTargetSelectionController.CohortKey> initialRequest = request(cohort, true, 20);
        TestTarget staleTarget = new TestTarget("stale-enemy", true);
        FormationTargetSelectionController.completeSelection(state, initialRequest, staleTarget);
        staleTarget.alive = false;

        FormationTargetSelectionController.Decision<TestTarget> invalidatedDecision = FormationTargetSelectionController.beginSelection(
                state,
                request(cohort, true, 25),
                TestTarget::alive
        );
        FormationTargetSelectionController.completeSelection(state, request(cohort, true, 25), null);
        FormationTargetSelectionController.Decision<TestTarget> sameTickDecision = FormationTargetSelectionController.beginSelection(
                state,
                request(cohort, true, 25),
                TestTarget::alive
        );
        FormationTargetSelectionController.Decision<TestTarget> nextTickDecision = FormationTargetSelectionController.beginSelection(
                state,
                request(cohort, true, 26),
                TestTarget::alive
        );

        FormationTargetSelectionController.ProfilingSnapshot snapshot = state.snapshot();

        assertEquals(FormationTargetSelectionController.DecisionType.COMPUTE_SHARED_SELECTION, invalidatedDecision.type());
        assertEquals(FormationTargetSelectionController.DecisionType.REUSED_SHARED_SELECTION, sameTickDecision.type());
        assertNull(sameTickDecision.target());
        assertEquals(FormationTargetSelectionController.DecisionType.COMPUTE_SHARED_SELECTION, nextTickDecision.type());
        assertEquals(1, snapshot.formationSelectionReuses());
        assertEquals(2, snapshot.formationSelectionInvalidations());
    }

    private static FormationTargetSelectionController.SelectionRequest<FormationTargetSelectionController.CohortKey> request(
            FormationTargetSelectionController.CohortKey cohort,
            boolean formationEligible,
            long gameTime
    ) {
        return new FormationTargetSelectionController.SelectionRequest<>(cohort, formationEligible, gameTime, 40);
    }

    private static final class TestTarget {
        private final String id;
        private boolean alive;

        private TestTarget(String id, boolean alive) {
            this.id = id;
            this.alive = alive;
        }

        private boolean alive() {
            return alive;
        }

        @Override
        public String toString() {
            return id;
        }
    }
}
