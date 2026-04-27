package com.talhanation.bannermod.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoralePolicyTest {

    private static MoraleSnapshot defaultSnapshot() {
        return new MoraleSnapshot(8, 0, 0, false, 0, 0, false);
    }

    @Test
    void calmSquadIsSteadyWithNoReasons() {
        MoraleAssessment assessment = MoralePolicy.evaluate(defaultSnapshot());

        assertEquals(MoraleState.STEADY, assessment.state());
        assertTrue(assessment.reasons().isEmpty());
    }

    @Test
    void heavyCasualtiesAndOutnumberedThreeXRoutsTheSquad() {
        // 8-strong squad, 4 casualties (50% -> heavy), 12 visible hostiles vs 4 survivors
        // (3:1 -> badly outnumbered): pressure = 3 (heavy) + 3 (3x) = 6 >= rout threshold.
        MoraleSnapshot snapshot = new MoraleSnapshot(8, 4, 12, false, 0, 0, false);

        MoraleAssessment assessment = MoralePolicy.evaluate(snapshot);

        assertEquals(MoraleState.ROUTED, assessment.state());
        assertTrue(assessment.reasons().contains("CASUALTIES_HEAVY"));
        assertTrue(assessment.reasons().contains("OUTNUMBERED_3X"));
    }

    @Test
    void commanderPresentDowngradesRoutToShaken() {
        // Same scenario as the rout test, but a commander is in aura range.
        MoraleSnapshot snapshot = new MoraleSnapshot(8, 4, 12, true, 0, 0, false);

        MoraleAssessment assessment = MoralePolicy.evaluate(snapshot);

        assertEquals(MoraleState.SHAKEN, assessment.state());
        assertTrue(assessment.reasons().contains("COMMANDER_PRESENT"));
    }

    @Test
    void isolatedShakenSquadDoesNotRecoverWithoutAllies() {
        // 25% casualties + isolated = pressure 2 -> SHAKEN, no nearby ally support.
        MoraleSnapshot snapshot = new MoraleSnapshot(8, 2, 0, false, 0, 0, true);

        MoraleAssessment assessment = MoralePolicy.evaluate(snapshot);

        assertEquals(MoraleState.SHAKEN, assessment.state());
        assertTrue(assessment.reasons().contains("CASUALTIES_MODERATE"));
        assertTrue(assessment.reasons().contains("ISOLATED"));
    }

    @Test
    void nearbyAlliesDowngradeShakenToSteadyAtLowPressure() {
        // Moderate casualties only (pressure 1) -> STEADY already; allies just confirm.
        MoraleSnapshot lowPressure = new MoraleSnapshot(8, 2, 0, false, 4, 0, false);
        assertEquals(MoraleState.STEADY, MoralePolicy.evaluate(lowPressure).state());

        // Moderate casualties + isolated (pressure 2) -> SHAKEN; allies downgrade to STEADY.
        MoraleSnapshot atShakenThreshold = new MoraleSnapshot(8, 2, 0, false, 0, 0, true);
        assertEquals(MoraleState.SHAKEN, MoralePolicy.evaluate(atShakenThreshold).state());

        MoraleSnapshot withAllies = new MoraleSnapshot(8, 2, 0, false, 4, 0, true);
        MoraleAssessment relieved = MoralePolicy.evaluate(withAllies);
        assertEquals(MoraleState.STEADY, relieved.state());
        assertTrue(relieved.reasons().contains("ALLIES_NEARBY"));
    }

    @Test
    void sustainedFireRaisesSuppressionPressure() {
        MoraleSnapshot snapshot = new MoraleSnapshot(
                8, 0, 0, false, 0,
                MoralePolicy.SUSTAINED_FIRE_THRESHOLD, true);

        MoraleAssessment assessment = MoralePolicy.evaluate(snapshot);

        // ISOLATED (1) + SUSTAINED_FIRE (1) = pressure 2 -> SHAKEN.
        assertEquals(MoraleState.SHAKEN, assessment.state());
        assertTrue(assessment.reasons().contains("SUSTAINED_FIRE"));
    }

    @Test
    void wipedSquadStillReportsOutnumberedPressure() {
        // All casualties; survivors == 0 but hostiles still visible. Policy must record the
        // pressure rather than NaN-divide on the outnumbered ratio.
        MoraleSnapshot snapshot = new MoraleSnapshot(8, 8, 5, false, 0, 0, true);

        MoraleAssessment assessment = MoralePolicy.evaluate(snapshot);

        assertEquals(MoraleState.ROUTED, assessment.state());
        assertTrue(assessment.reasons().contains("CASUALTIES_HEAVY"));
        assertTrue(assessment.reasons().contains("OUTNUMBERED_3X"));
    }

    @Test
    void clampsRejectNegativeAndOverflowInputs() {
        // Negative values clamped to zero / one; over-cap casualties clamped to squadSize.
        MoraleSnapshot snapshot = new MoraleSnapshot(-3, -10, -2, false, -7, -1, false);
        assertEquals(1, snapshot.squadSize());
        assertEquals(0, snapshot.casualtiesTaken());
        assertEquals(0, snapshot.hostileVisibleSize());
        assertEquals(0, snapshot.nearbyAllyCount());
        assertEquals(0, snapshot.recentDamageEvents());

        MoraleSnapshot overCapCasualties = new MoraleSnapshot(4, 99, 0, false, 0, 0, false);
        assertEquals(4, overCapCasualties.casualtiesTaken());
        assertEquals(0, overCapCasualties.survivors());
    }

    @Test
    void nullSnapshotDefaultsToSteady() {
        MoraleAssessment assessment = MoralePolicy.evaluate(null);

        assertEquals(MoraleState.STEADY, assessment.state());
        assertTrue(assessment.reasons().isEmpty());
    }

    @Test
    void casualtyAndOutnumberedReasonOrderIsStable() {
        // Reason order matters for deterministic UI/audit formatting.
        MoraleSnapshot snapshot = new MoraleSnapshot(8, 4, 12, true, 0,
                MoralePolicy.SUSTAINED_FIRE_THRESHOLD, true);

        MoraleAssessment assessment = MoralePolicy.evaluate(snapshot);

        // Pressure tokens come first in evaluation order, relief tokens last.
        java.util.List<String> reasons = assessment.reasons();
        assertEquals("CASUALTIES_HEAVY", reasons.get(0));
        assertEquals("OUTNUMBERED_3X", reasons.get(1));
        assertEquals("ISOLATED", reasons.get(2));
        assertEquals("SUSTAINED_FIRE", reasons.get(3));
        assertEquals("COMMANDER_PRESENT", reasons.get(4));
    }
}
