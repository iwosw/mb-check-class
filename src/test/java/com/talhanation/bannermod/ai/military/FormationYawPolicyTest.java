package com.talhanation.bannermod.ai.military;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step 1.D: slow body-turn in formation under LINE_HOLD / SHIELD_WALL.
 */
class FormationYawPolicyTest {

    private static final float EPS = 1e-4f;

    @Test
    void looseStanceHasNoYawClamp() {
        assertTrue(Float.isNaN(FormationYawPolicy.perTickBodyYawLimitDegrees(CombatStance.LOOSE)));
        assertTrue(Float.isNaN(FormationYawPolicy.perTickBodyYawLimitDegrees(null)));
    }

    @Test
    void lineHoldClampsTo10Degrees() {
        assertEquals(10f, FormationYawPolicy.perTickBodyYawLimitDegrees(CombatStance.LINE_HOLD), EPS);
    }

    @Test
    void shieldWallClampsTo6Degrees() {
        assertEquals(6f, FormationYawPolicy.perTickBodyYawLimitDegrees(CombatStance.SHIELD_WALL), EPS);
    }

    @Test
    void clampLimitsPositiveDeltaToLimit() {
        // Asking to rotate 30 degrees with a 10-degree cap must cap at 10.
        float clamped = FormationYawPolicy.clampBodyYaw(0f, 30f, 10f);
        assertEquals(10f, clamped, EPS);
    }

    @Test
    void clampLimitsNegativeDeltaToNegativeLimit() {
        float clamped = FormationYawPolicy.clampBodyYaw(0f, -30f, 10f);
        assertEquals(-10f, clamped, EPS);
    }

    @Test
    void clampLeavesSmallDeltaUntouched() {
        float clamped = FormationYawPolicy.clampBodyYaw(0f, 3f, 10f);
        assertEquals(3f, clamped, EPS);
    }

    @Test
    void clampHandlesYawWraparound() {
        // 179 -> -179 is a +2-degree step (the short way), not -358.
        float clamped = FormationYawPolicy.clampBodyYaw(179f, -179f, 10f);
        assertEquals(181f, clamped, EPS);
    }

    @Test
    void clampHandlesYawWraparoundAtLimit() {
        // 170 -> -170 is a +20-degree step (short way); cap at 10 -> 170 + 10 = 180.
        float clamped = FormationYawPolicy.clampBodyYaw(170f, -170f, 10f);
        assertEquals(180f, clamped, EPS);
    }

    @Test
    void clampWithZeroOrNaNLimitPassesThrough() {
        assertEquals(30f, FormationYawPolicy.clampBodyYaw(0f, 30f, 0f), EPS);
        assertEquals(30f, FormationYawPolicy.clampBodyYaw(0f, 30f, Float.NaN), EPS);
    }
}
