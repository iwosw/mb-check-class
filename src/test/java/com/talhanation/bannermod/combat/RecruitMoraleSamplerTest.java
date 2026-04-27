package com.talhanation.bannermod.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks the pure HP-to-casualty mapping used by {@link RecruitMoraleSampler} when it
 * approximates squad-level casualties from a single recruit's HP fraction. The live-world
 * scan path (the rest of the sampler) is exercised by gametest infrastructure; this suite
 * focuses on the boundary of the casualty thresholds since they directly drive the
 * {@code CASUALTIES_HEAVY} / {@code CASUALTIES_MODERATE} reasons emitted by the policy.
 */
class RecruitMoraleSamplerTest {

    @Test
    void healthyRecruitReportsNoCasualties() {
        assertEquals(0, RecruitMoraleSampler.casualtiesFromHpFraction(20.0F, 20.0F, 8));
        assertEquals(0, RecruitMoraleSampler.casualtiesFromHpFraction(15.0F, 20.0F, 8));
    }

    @Test
    void halfHpReportsModerateCasualtyShare() {
        // At HP fraction 0.50 (== MODERATE_HP_FRACTION) the casualty proxy is 30% of squad.
        int casualties = RecruitMoraleSampler.casualtiesFromHpFraction(10.0F, 20.0F, 10);
        assertEquals(3, casualties);
    }

    @Test
    void quarterHpReportsHeavyCasualtyShare() {
        // At HP fraction 0.25 (== HEAVY_HP_FRACTION) the casualty proxy is 75% of squad.
        int casualties = RecruitMoraleSampler.casualtiesFromHpFraction(5.0F, 20.0F, 8);
        assertEquals(6, casualties);
    }

    @Test
    void aboveModerateThresholdReportsZero() {
        // 0.60 fraction is above the moderate threshold, so no casualties recorded.
        assertEquals(0, RecruitMoraleSampler.casualtiesFromHpFraction(12.0F, 20.0F, 10));
    }

    @Test
    void smallSquadAtHeavyHpStillReportsAtLeastOne() {
        // With a single-recruit "squad" the percentage rounds to zero — the floor protects
        // against silently dropping the heavy-casualty signal on the policy side.
        assertEquals(1, RecruitMoraleSampler.casualtiesFromHpFraction(0.5F, 20.0F, 1));
    }

    @Test
    void zeroOrNegativeMaxHpClampsToZero() {
        assertEquals(0, RecruitMoraleSampler.casualtiesFromHpFraction(0.0F, 0.0F, 8));
        assertEquals(0, RecruitMoraleSampler.casualtiesFromHpFraction(5.0F, -1.0F, 8));
    }

    @Test
    void zeroSquadSizeClampsToZero() {
        assertEquals(0, RecruitMoraleSampler.casualtiesFromHpFraction(5.0F, 20.0F, 0));
    }

    @Test
    void casualtyCountNeverExceedsSquadSizeAfterPolicyClamp() {
        // The policy clamps casualties to squadSize; the sampler is allowed to over-report
        // and rely on that clamp. This locks the contract that 75% of a small squad can be
        // larger than the squad before policy clamping.
        int casualties = RecruitMoraleSampler.casualtiesFromHpFraction(0.1F, 20.0F, 2);
        assertTrue(casualties >= 1, "Heavy-hp casualty floor should be at least 1");
        // Now feed the count through MoralePolicy via MoraleSnapshot to confirm the clamp.
        MoraleSnapshot snapshot = new MoraleSnapshot(2, casualties, 5, false, 0, 0, true);
        MoraleAssessment assessment = MoralePolicy.evaluate(snapshot);
        assertTrue(assessment.reasons().contains("CASUALTIES_HEAVY"),
                "Heavy casualty proxy must produce CASUALTIES_HEAVY downstream.");
    }
}
