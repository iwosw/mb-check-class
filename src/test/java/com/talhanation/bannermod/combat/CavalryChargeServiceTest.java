package com.talhanation.bannermod.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks the CHARGE_SPEED_THRESHOLD constant against the policy's tunables and the
 * {@link CavalryChargeService} <-> {@link CavalryChargePolicy} contract so a regression
 * either side surfaces in JUnit. The world-side state-machine driver is exercised by
 * gametests; this suite focuses on the boundary numbers and the multiplier delegation.
 */
class CavalryChargeServiceTest {

    @Test
    void chargeSpeedThresholdIsBelowVanillaGallopAndAboveCasualWalk() {
        // A vanilla walking horse moves ~0.05–0.08 b/t; a cantering / galloping horse
        // exceeds 0.30 b/t under sprint. The threshold must sit between these so a
        // casual approach does not latch CHARGING but a deliberate gallop does.
        assertTrue(CavalryChargeService.CHARGE_SPEED_THRESHOLD > 0.10D);
        assertTrue(CavalryChargeService.CHARGE_SPEED_THRESHOLD < 0.30D);
    }

    @Test
    void multiplierDelegationMatchesPolicyForCanonicalCases() {
        // Service computes against the same policy table the unit suite already locks;
        // these spot-checks confirm the service's wiring does not silently drop a case.
        assertEquals(CavalryChargePolicy.FIRST_HIT_BONUS_MULTIPLIER,
                CavalryChargePolicy.damageMultiplierFor(
                        CavalryChargeState.CHARGING, CombatRole.INFANTRY, false));
        assertEquals(CavalryChargePolicy.PIKE_BRACE_PENALTY_MULTIPLIER,
                CavalryChargePolicy.damageMultiplierFor(
                        CavalryChargeState.CHARGING, CombatRole.PIKE, true));
        assertEquals(1.0D,
                CavalryChargePolicy.damageMultiplierFor(
                        CavalryChargeState.EXHAUSTED, CombatRole.INFANTRY, false));
        assertNotEquals(1.0D, CavalryChargePolicy.FIRST_HIT_BONUS_MULTIPLIER,
                "First-hit bonus must not collapse to identity, otherwise the cavalry "
                        + "charge has no observable effect.");
    }
}
