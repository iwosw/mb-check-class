package com.talhanation.bannermod.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RangedSpacingPolicyTest {

    @Test
    void wellSpacedRangedUnitWithClearLaneStays() {
        // Far from enemy, far from own melee, lane clear -> STAY.
        assertEquals(RangedAction.STAY,
                RangedSpacingPolicy.decide(20.0D, 12.0D, false));
    }

    @Test
    void closeEnemyMeleeForcesFallback() {
        // Enemy inside 6-block fallback radius even though friendly buffer is fine.
        assertEquals(RangedAction.FALLBACK,
                RangedSpacingPolicy.decide(2.0D, 12.0D, false));
    }

    @Test
    void boundaryEnemyDistanceIsNotConsideredClose() {
        // Exactly at radius -> NOT close (strict less-than). Acceptable since we'd rather
        // hold position than thrash on the boundary.
        assertEquals(RangedAction.STAY,
                RangedSpacingPolicy.decide(RangedSpacingPolicy.ENEMY_FALLBACK_RADIUS, 12.0D, false));
    }

    @Test
    void collapsedFriendlyLineForcesFallback() {
        // Enemy is far, but friendly melee collapsed inside the buffer -> FALLBACK to
        // re-establish rear-rank spacing.
        assertEquals(RangedAction.FALLBACK,
                RangedSpacingPolicy.decide(20.0D, 1.5D, false));
    }

    @Test
    void firingLaneBlockedSidestepsLaterally() {
        // Distances are good, but an ally is in the firing cone -> LATERAL_SHIFT.
        assertEquals(RangedAction.LATERAL_SHIFT,
                RangedSpacingPolicy.decide(20.0D, 12.0D, true));
    }

    @Test
    void enemyFallbackBeatsLateralShiftOrdering() {
        // Both an enemy is close AND the lane is blocked. Fallback takes priority because
        // the unit will likely die before any shot lands if it sidesteps instead.
        assertEquals(RangedAction.FALLBACK,
                RangedSpacingPolicy.decide(2.0D, 12.0D, true));
    }

    @Test
    void friendlyFallbackBeatsLateralShiftOrdering() {
        // Friendly buffer breached + lane blocked: fallback wins so the unit is not stuck
        // sidestepping into its own melee line.
        assertEquals(RangedAction.FALLBACK,
                RangedSpacingPolicy.decide(20.0D, 1.5D, true));
    }

    @Test
    void infiniteDistancesActAsNoConstraint() {
        // Caller passes POSITIVE_INFINITY when no actor was found in range. Policy must
        // not emit FALLBACK on those.
        assertEquals(RangedAction.STAY,
                RangedSpacingPolicy.decide(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, false));
        assertEquals(RangedAction.LATERAL_SHIFT,
                RangedSpacingPolicy.decide(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, true));
    }

    @Test
    void negativeOrNanDistancesAreTreatedAsNoConstraint() {
        // Defensive: a buggy caller passing -1 or NaN must not flip the policy into a
        // permanent fallback.
        assertEquals(RangedAction.STAY,
                RangedSpacingPolicy.decide(-1.0D, -1.0D, false));
        assertEquals(RangedAction.STAY,
                RangedSpacingPolicy.decide(Double.NaN, Double.NaN, false));
    }
}
