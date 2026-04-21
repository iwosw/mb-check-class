package com.talhanation.bannermod.ai.military;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step 2.A/B: stance-aware shield damage mitigation with stagger cooldown.
 */
class ShieldMitigationTest {

    private static final float EPS = 1e-4f;

    @Test
    void noMitigationWhenShieldDown() {
        float got = ShieldMitigation.damageAfterBlock(CombatStance.SHIELD_WALL, 10f, true, false, false);
        assertEquals(10f, got, EPS);
    }

    @Test
    void noMitigationWhenOutOfCone() {
        float got = ShieldMitigation.damageAfterBlock(CombatStance.SHIELD_WALL, 10f, false, true, false);
        assertEquals(10f, got, EPS);
    }

    @Test
    void looseStanceAbsorbsAboutFortyFivePercent() {
        float got = ShieldMitigation.damageAfterBlock(CombatStance.LOOSE, 10f, true, true, false);
        assertEquals(10f * ShieldMitigation.LOOSE_REMAINING, got, EPS);
    }

    @Test
    void lineHoldStanceAbsorbsMoreThanLoose() {
        float got = ShieldMitigation.damageAfterBlock(CombatStance.LINE_HOLD, 10f, true, true, false);
        assertEquals(10f * ShieldMitigation.LINE_HOLD_REMAINING, got, EPS);
    }

    @Test
    void shieldWallAbsorbsMoreThanLineHold() {
        float got = ShieldMitigation.damageAfterBlock(CombatStance.SHIELD_WALL, 10f, true, true, false);
        assertEquals(10f * ShieldMitigation.SHIELD_WALL_REMAINING, got, EPS);
    }

    @Test
    void nullStanceFallsBackToLoose() {
        float got = ShieldMitigation.damageAfterBlock(null, 10f, true, true, false);
        assertEquals(10f * ShieldMitigation.LOOSE_REMAINING, got, EPS);
    }

    @Test
    void staggerReducesAbsorption() {
        // Without stagger SHIELD_WALL remaining = 0.30 -> 3.0 damage.
        // With stagger, absorbed fraction (0.70) is reduced by 40% -> 0.42, so
        // remaining = 0.58 -> 5.80 damage.
        float nominal = ShieldMitigation.damageAfterBlock(CombatStance.SHIELD_WALL, 10f, true, true, false);
        float staggered = ShieldMitigation.damageAfterBlock(CombatStance.SHIELD_WALL, 10f, true, true, true);
        float expectedStaggered = 10f * (1f - (1f - ShieldMitigation.SHIELD_WALL_REMAINING) * (1f - ShieldMitigation.STAGGER_REDUCTION));
        assertEquals(expectedStaggered, staggered, EPS);
        assertEquals(true, staggered > nominal);
    }

    @Test
    void zeroOrNegativeDamagePassesThrough() {
        assertEquals(0f, ShieldMitigation.damageAfterBlock(CombatStance.SHIELD_WALL, 0f, true, true, false), EPS);
        assertEquals(-1.5f, ShieldMitigation.damageAfterBlock(CombatStance.SHIELD_WALL, -1.5f, true, true, false), EPS);
    }

    @Test
    void remainingFractionNullStanceMatchesLoose() {
        assertEquals(ShieldMitigation.LOOSE_REMAINING, ShieldMitigation.remainingFractionFor(null), EPS);
    }
}
