package com.talhanation.bannermod.ai.military;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Stage 4.C: bracing against cavalry charge. */
class BraceAgainstChargePolicyTest {

    private static final double NEAR = BraceAgainstChargePolicy.BRACE_RADIUS_SQR - 1;
    private static final double FAR = BraceAgainstChargePolicy.BRACE_RADIUS_SQR + 10;

    @Test
    void bracesWhenShieldmanSeesMountedHostileNearby() {
        List<BraceAgainstChargePolicy.HostileObservation> hostiles = List.of(
                new BraceAgainstChargePolicy.HostileObservation(NEAR, true, true)
        );
        assertTrue(BraceAgainstChargePolicy.shouldBrace(CombatStance.LINE_HOLD, true, false, hostiles));
    }

    @Test
    void bracesWhenReachWeaponHolderSeesMountedHostile() {
        List<BraceAgainstChargePolicy.HostileObservation> hostiles = List.of(
                new BraceAgainstChargePolicy.HostileObservation(NEAR, true, true)
        );
        assertTrue(BraceAgainstChargePolicy.shouldBrace(CombatStance.SHIELD_WALL, false, true, hostiles));
    }

    @Test
    void looseStanceNeverBraces() {
        List<BraceAgainstChargePolicy.HostileObservation> hostiles = List.of(
                new BraceAgainstChargePolicy.HostileObservation(NEAR, true, true)
        );
        assertFalse(BraceAgainstChargePolicy.shouldBrace(CombatStance.LOOSE, true, true, hostiles));
    }

    @Test
    void nullStanceNeverBraces() {
        List<BraceAgainstChargePolicy.HostileObservation> hostiles = List.of(
                new BraceAgainstChargePolicy.HostileObservation(NEAR, true, true)
        );
        assertFalse(BraceAgainstChargePolicy.shouldBrace(null, true, true, hostiles));
    }

    @Test
    void noShieldOrReachNoBrace() {
        List<BraceAgainstChargePolicy.HostileObservation> hostiles = List.of(
                new BraceAgainstChargePolicy.HostileObservation(NEAR, true, true)
        );
        assertFalse(BraceAgainstChargePolicy.shouldBrace(CombatStance.LINE_HOLD, false, false, hostiles));
    }

    @Test
    void dismountedHostileDoesNotTriggerBrace() {
        List<BraceAgainstChargePolicy.HostileObservation> hostiles = List.of(
                new BraceAgainstChargePolicy.HostileObservation(NEAR, false, true)
        );
        assertFalse(BraceAgainstChargePolicy.shouldBrace(CombatStance.LINE_HOLD, true, false, hostiles));
    }

    @Test
    void distantMountedHostileDoesNotTriggerBrace() {
        List<BraceAgainstChargePolicy.HostileObservation> hostiles = List.of(
                new BraceAgainstChargePolicy.HostileObservation(FAR, true, true)
        );
        assertFalse(BraceAgainstChargePolicy.shouldBrace(CombatStance.LINE_HOLD, true, false, hostiles));
    }

    @Test
    void emptyHostilesDoesNotTriggerBrace() {
        assertFalse(BraceAgainstChargePolicy.shouldBrace(CombatStance.LINE_HOLD, true, true, List.of()));
    }

    @Test
    void nullHostilesDoesNotTriggerBrace() {
        assertFalse(BraceAgainstChargePolicy.shouldBrace(CombatStance.LINE_HOLD, true, true, null));
    }

    @Test
    void notApproachingDoesNotTriggerBrace() {
        List<BraceAgainstChargePolicy.HostileObservation> hostiles = List.of(
                new BraceAgainstChargePolicy.HostileObservation(NEAR, true, false)
        );
        assertFalse(BraceAgainstChargePolicy.shouldBrace(CombatStance.LINE_HOLD, true, false, hostiles));
    }
}
