package com.talhanation.bannermod.ai.military;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step 1.B: formation-aware pursuit leash.
 */
class CombatLeashPolicyTest {

    @Test
    void looseStanceKeepsPreviousFormationLeashOf13Blocks() {
        assertEquals(169.0D, CombatLeashPolicy.maxEngageDistanceSqr(CombatStance.LOOSE, true));
    }

    @Test
    void outOfFormationAlwaysUses20BlockLeash() {
        assertEquals(400.0D, CombatLeashPolicy.maxEngageDistanceSqr(CombatStance.LOOSE, false));
        assertEquals(400.0D, CombatLeashPolicy.maxEngageDistanceSqr(CombatStance.LINE_HOLD, false));
        assertEquals(400.0D, CombatLeashPolicy.maxEngageDistanceSqr(CombatStance.SHIELD_WALL, false));
    }

    @Test
    void lineHoldShrinksFormationLeashTo5Blocks() {
        assertEquals(25.0D, CombatLeashPolicy.maxEngageDistanceSqr(CombatStance.LINE_HOLD, true));
    }

    @Test
    void shieldWallShrinksFormationLeashTo3Blocks() {
        assertEquals(9.0D, CombatLeashPolicy.maxEngageDistanceSqr(CombatStance.SHIELD_WALL, true));
    }

    @Test
    void canEngageReturnsTrueWhenNoHoldPos() {
        assertTrue(CombatLeashPolicy.canEngage(10_000D, false, true, CombatStance.SHIELD_WALL));
    }

    @Test
    void canEngageBlocksDistantTargetsUnderLineHold() {
        // 6-block target in LINE_HOLD: 36 > 25 => block
        assertFalse(CombatLeashPolicy.canEngage(36D, true, true, CombatStance.LINE_HOLD));
    }

    @Test
    void canEngageAllowsNearbyTargetsUnderLineHold() {
        // 4-block target in LINE_HOLD: 16 < 25 => allow
        assertTrue(CombatLeashPolicy.canEngage(16D, true, true, CombatStance.LINE_HOLD));
    }

    @Test
    void canEngageAllowsLegitimate12BlockChaseInLooseStance() {
        // 12-block target in LOOSE: 144 < 169 => allow (don't break LOOSE behaviour).
        assertTrue(CombatLeashPolicy.canEngage(144D, true, true, CombatStance.LOOSE));
    }

    @Test
    void hasDriftedOffLeashOnlyFiresInsideFormationAndTightStance() {
        // LOOSE must never report drift — preserves pre-Stage-1 chasing.
        assertFalse(CombatLeashPolicy.hasDriftedOffLeash(10_000D, true, true, CombatStance.LOOSE));
        // Out-of-formation never reports drift.
        assertFalse(CombatLeashPolicy.hasDriftedOffLeash(10_000D, true, false, CombatStance.SHIELD_WALL));
        // No hold-pos never reports drift.
        assertFalse(CombatLeashPolicy.hasDriftedOffLeash(10_000D, false, true, CombatStance.SHIELD_WALL));
    }

    @Test
    void hasDriftedOffLeashFiresWhenSelfExceedsStanceLeash() {
        // In LINE_HOLD, 6-block drift (36) > 25 => drift.
        assertTrue(CombatLeashPolicy.hasDriftedOffLeash(36D, true, true, CombatStance.LINE_HOLD));
        // In SHIELD_WALL, 4-block drift (16) > 9 => drift.
        assertTrue(CombatLeashPolicy.hasDriftedOffLeash(16D, true, true, CombatStance.SHIELD_WALL));
        // In SHIELD_WALL, 2.5-block drift (6.25) < 9 => no drift.
        assertFalse(CombatLeashPolicy.hasDriftedOffLeash(6.25D, true, true, CombatStance.SHIELD_WALL));
    }
}
