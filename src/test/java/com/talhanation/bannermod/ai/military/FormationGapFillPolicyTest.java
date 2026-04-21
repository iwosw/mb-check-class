package com.talhanation.bannermod.ai.military;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step 1.C: pick a forward (or adjacent) empty slot when a neighbour dies.
 */
class FormationGapFillPolicyTest {

    private static final double EPS = 1e-4;

    @Test
    void looseStanceDoesNotFillGaps() {
        assertFalse(FormationGapFillPolicy.stanceAllowsGapFill(CombatStance.LOOSE));
        assertFalse(FormationGapFillPolicy.stanceAllowsGapFill(null));
    }

    @Test
    void lineHoldAndShieldWallFillGaps() {
        assertTrue(FormationGapFillPolicy.stanceAllowsGapFill(CombatStance.LINE_HOLD));
        assertTrue(FormationGapFillPolicy.stanceAllowsGapFill(CombatStance.SHIELD_WALL));
    }

    @Test
    void choosesStrictlyForwardEmptySlotOverEqualRankGap() {
        // self at rank 1, column 0. Dead slot forward (rank 0, column 0) should be picked
        // over a dead slot on the same rank (rank 1, column -1).
        FormationGapFillPolicy.LocalSlot self = new FormationGapFillPolicy.LocalSlot(10, 0, 0, true);
        FormationGapFillPolicy.LocalSlot forwardGap = new FormationGapFillPolicy.LocalSlot(1, 0, -1, false);
        FormationGapFillPolicy.LocalSlot sidewaysGap = new FormationGapFillPolicy.LocalSlot(9, -1, 0, false);
        FormationGapFillPolicy.LocalSlot occupiedBehind = new FormationGapFillPolicy.LocalSlot(20, 0, 1, true);

        Optional<Integer> chosen = FormationGapFillPolicy.chooseGapSlot(
                self,
                List.of(self, forwardGap, sidewaysGap, occupiedBehind)
        );
        assertEquals(Optional.of(1), chosen);
    }

    @Test
    void fallsBackToAdjacentGapWhenNoForwardGap() {
        FormationGapFillPolicy.LocalSlot self = new FormationGapFillPolicy.LocalSlot(10, 0, 0, true);
        // No forward gap — only a sideways slot is empty.
        FormationGapFillPolicy.LocalSlot sidewaysGap = new FormationGapFillPolicy.LocalSlot(9, -1, 0, false);
        FormationGapFillPolicy.LocalSlot occupiedForward = new FormationGapFillPolicy.LocalSlot(1, 0, -1, true);

        Optional<Integer> chosen = FormationGapFillPolicy.chooseGapSlot(
                self,
                List.of(self, sidewaysGap, occupiedForward)
        );
        assertEquals(Optional.of(9), chosen);
    }

    @Test
    void doesNotFillBehindGapBeyondAdjacentDistance() {
        FormationGapFillPolicy.LocalSlot self = new FormationGapFillPolicy.LocalSlot(10, 0, 0, true);
        // Only empty slot is 3 ranks behind — outside adjacent range.
        FormationGapFillPolicy.LocalSlot farBehindGap = new FormationGapFillPolicy.LocalSlot(99, 0, 3, false);

        Optional<Integer> chosen = FormationGapFillPolicy.chooseGapSlot(
                self,
                List.of(self, farBehindGap)
        );
        assertTrue(chosen.isEmpty());
    }

    @Test
    void picksNearestForwardGapWhenMultiplePresent() {
        FormationGapFillPolicy.LocalSlot self = new FormationGapFillPolicy.LocalSlot(10, 0, 0, true);
        FormationGapFillPolicy.LocalSlot nearForwardGap = new FormationGapFillPolicy.LocalSlot(5, 0, -1, false);
        FormationGapFillPolicy.LocalSlot farForwardGap = new FormationGapFillPolicy.LocalSlot(2, 0, -3, false);

        Optional<Integer> chosen = FormationGapFillPolicy.chooseGapSlot(
                self,
                List.of(self, nearForwardGap, farForwardGap)
        );
        assertEquals(Optional.of(5), chosen);
    }

    @Test
    void returnsEmptyWhenNoGaps() {
        FormationGapFillPolicy.LocalSlot self = new FormationGapFillPolicy.LocalSlot(10, 0, 0, true);
        FormationGapFillPolicy.LocalSlot occupied = new FormationGapFillPolicy.LocalSlot(5, 0, -1, true);

        Optional<Integer> chosen = FormationGapFillPolicy.chooseGapSlot(self, List.of(self, occupied));
        assertTrue(chosen.isEmpty());
    }

    @Test
    void worldDeltaToLocalAlignsForwardWithNegativeLocalY() {
        // ownerRot = 0 means forward = (0, 0, 1). A slot 5 blocks in +Z direction is "forward"
        // (toward the enemy) so localY should be negative.
        double[] localForward = FormationGapFillPolicy.worldDeltaToLocal(0, 5, 0f);
        assertEquals(0, localForward[0], EPS);
        assertEquals(-5, localForward[1], EPS);

        // A slot to the +X direction at yaw=0: forward = +Z, so "right" = +X, giving +localX.
        double[] localRight = FormationGapFillPolicy.worldDeltaToLocal(5, 0, 0f);
        assertEquals(5, localRight[0], EPS);
        assertEquals(0, localRight[1], EPS);
    }
}
