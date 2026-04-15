package com.talhanation.recruits.entities.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecruitAiLodPolicyTest {
    private static final RecruitAiLodPolicy.Settings DEFAULT_SETTINGS = new RecruitAiLodPolicy.Settings(true, 16, 40, 40, 80);

    @Test
    void closeOrRecentlyEngagedRecruitsStayFullFidelity() {
        RecruitAiLodPolicy.Evaluation closePlayerEvaluation = RecruitAiLodPolicy.evaluate(
                new RecruitAiLodPolicy.Context(false, false, Double.POSITIVE_INFINITY, 16.0D * 16.0D, 20, 0),
                DEFAULT_SETTINGS
        );
        RecruitAiLodPolicy.Evaluation recentDamageEvaluation = RecruitAiLodPolicy.evaluate(
                new RecruitAiLodPolicy.Context(true, false, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 21, 3),
                DEFAULT_SETTINGS
        );

        assertEquals(RecruitAiLodPolicy.LodTier.FULL, closePlayerEvaluation.tier());
        assertEquals(RecruitAiLodPolicy.DEFAULT_FULL_SEARCH_INTERVAL, closePlayerEvaluation.searchInterval());
        assertTrue(closePlayerEvaluation.shouldRunSearch());
        assertEquals(RecruitAiLodPolicy.LodTier.FULL, recentDamageEvaluation.tier());
    }

    @Test
    void distantLiveTargetsReduceWhileFullyUninvolvedRecruitsShed() {
        RecruitAiLodPolicy.Evaluation reducedEvaluation = RecruitAiLodPolicy.evaluate(
                new RecruitAiLodPolicy.Context(false, true, 30.0D * 30.0D, Double.POSITIVE_INFINITY, 40, 0),
                DEFAULT_SETTINGS
        );
        RecruitAiLodPolicy.Evaluation shedEvaluation = RecruitAiLodPolicy.evaluate(
                new RecruitAiLodPolicy.Context(false, false, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 40, 0),
                DEFAULT_SETTINGS
        );

        assertEquals(RecruitAiLodPolicy.LodTier.REDUCED, reducedEvaluation.tier());
        assertEquals(40, reducedEvaluation.searchInterval());
        assertTrue(reducedEvaluation.shouldRunSearch());
        assertEquals(RecruitAiLodPolicy.LodTier.SHED, shedEvaluation.tier());
        assertEquals(80, shedEvaluation.searchInterval());
        assertFalse(shedEvaluation.shouldRunSearch());
    }

    @Test
    void cadenceIsDeterministicForEachTier() {
        RecruitAiLodPolicy.Evaluation fullEvaluation = RecruitAiLodPolicy.evaluate(
                new RecruitAiLodPolicy.Context(false, false, 8.0D * 8.0D, Double.POSITIVE_INFINITY, 20, 0),
                DEFAULT_SETTINGS
        );
        RecruitAiLodPolicy.Evaluation reducedSkippedEvaluation = RecruitAiLodPolicy.evaluate(
                new RecruitAiLodPolicy.Context(false, true, 25.0D * 25.0D, Double.POSITIVE_INFINITY, 20, 0),
                DEFAULT_SETTINGS
        );
        RecruitAiLodPolicy.Evaluation shedRunEvaluation = RecruitAiLodPolicy.evaluate(
                new RecruitAiLodPolicy.Context(false, false, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 80, 0),
                DEFAULT_SETTINGS
        );

        assertEquals(RecruitAiLodPolicy.DEFAULT_FULL_SEARCH_INTERVAL, fullEvaluation.searchInterval());
        assertTrue(fullEvaluation.shouldRunSearch());
        assertEquals(40, reducedSkippedEvaluation.searchInterval());
        assertFalse(reducedSkippedEvaluation.shouldRunSearch());
        assertEquals(80, shedRunEvaluation.searchInterval());
        assertTrue(shedRunEvaluation.shouldRunSearch());
    }

    @Test
    void disabledPolicyFallsBackToFullTier() {
        RecruitAiLodPolicy.Evaluation evaluation = RecruitAiLodPolicy.evaluate(
                new RecruitAiLodPolicy.Context(false, false, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 20, 0),
                new RecruitAiLodPolicy.Settings(false, 16, 40, 40, 80)
        );

        assertEquals(RecruitAiLodPolicy.LodTier.FULL, evaluation.tier());
        assertEquals(RecruitAiLodPolicy.DEFAULT_FULL_SEARCH_INTERVAL, evaluation.searchInterval());
        assertTrue(evaluation.shouldRunSearch());
    }
}
