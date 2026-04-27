package com.talhanation.bannermod.combat;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SiegeObjectivePolicyTest {

    private static final UUID FRIENDLY = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID HOSTILE = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void opposingSideAttackerMayEngageStandard() {
        assertTrue(SiegeObjectivePolicy.canAttackStandard(HOSTILE, FRIENDLY));
        assertTrue(SiegeObjectivePolicy.canAttackStandard(FRIENDLY, HOSTILE));
    }

    @Test
    void sameSideAttackerMayNotEngageStandard() {
        assertFalse(SiegeObjectivePolicy.canAttackStandard(FRIENDLY, FRIENDLY));
    }

    @Test
    void unaffiliatedAttackerMayNotEngageStandard() {
        assertFalse(SiegeObjectivePolicy.canAttackStandard(null, FRIENDLY));
        assertFalse(SiegeObjectivePolicy.canAttackStandard(FRIENDLY, null));
    }

    @Test
    void escortGuardOnlyAppliesToSameSide() {
        assertTrue(SiegeObjectivePolicy.shouldEscort(FRIENDLY, FRIENDLY));
        assertFalse(SiegeObjectivePolicy.shouldEscort(FRIENDLY, HOSTILE));
        assertFalse(SiegeObjectivePolicy.shouldEscort(null, FRIENDLY));
        assertFalse(SiegeObjectivePolicy.shouldEscort(FRIENDLY, null));
    }

    @Test
    void damageReducesControlPool() {
        SiegeObjectivePolicy.DamageOutcome outcome = SiegeObjectivePolicy.applyDamage(50, 20, 100);

        assertEquals(30, outcome.controlAfter());
        assertFalse(outcome.destroyed());
    }

    @Test
    void damageThatDrainsThePoolFlagsDestruction() {
        SiegeObjectivePolicy.DamageOutcome outcome = SiegeObjectivePolicy.applyDamage(10, 50, 100);

        assertEquals(0, outcome.controlAfter());
        assertTrue(outcome.destroyed());
    }

    @Test
    void damageOnAnAlreadyDestroyedStandardDoesNotReFlagDestruction() {
        // The audit log must record exactly one destruction event, so a follow-up damage
        // tick on a 0-control standard must not produce destroyed=true again.
        SiegeObjectivePolicy.DamageOutcome outcome = SiegeObjectivePolicy.applyDamage(0, 25, 100);

        assertEquals(0, outcome.controlAfter());
        assertFalse(outcome.destroyed());
    }

    @Test
    void zeroDamageIsANoOpEvenAtZeroControl() {
        assertEquals(0, SiegeObjectivePolicy.applyDamage(0, 0, 100).controlAfter());
        assertFalse(SiegeObjectivePolicy.applyDamage(0, 0, 100).destroyed());
        assertEquals(50, SiegeObjectivePolicy.applyDamage(50, 0, 100).controlAfter());
        assertFalse(SiegeObjectivePolicy.applyDamage(50, 0, 100).destroyed());
    }

    @Test
    void clampsRejectNegativeAndOverCapInputs() {
        // currentControl > maxControl clamped to maxControl.
        SiegeObjectivePolicy.DamageOutcome overCap = SiegeObjectivePolicy.applyDamage(999, 10, 100);
        assertEquals(90, overCap.controlAfter());

        // Negative damage clamped to zero.
        SiegeObjectivePolicy.DamageOutcome negativeDamage = SiegeObjectivePolicy.applyDamage(50, -100, 100);
        assertEquals(50, negativeDamage.controlAfter());
        assertFalse(negativeDamage.destroyed());

        // Negative current clamped to zero.
        SiegeObjectivePolicy.DamageOutcome negativeCurrent = SiegeObjectivePolicy.applyDamage(-5, 10, 100);
        assertEquals(0, negativeCurrent.controlAfter());
        assertFalse(negativeCurrent.destroyed());
    }

    @Test
    void damageBeyondPoolClampsToZero() {
        SiegeObjectivePolicy.DamageOutcome outcome = SiegeObjectivePolicy.applyDamage(20, 9999, 100);
        assertEquals(0, outcome.controlAfter());
        assertTrue(outcome.destroyed());
    }
}
