package com.talhanation.bannermod.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CavalryChargePolicyTest {

    @Test
    void chargingIntoUnbracedInfantryLandsTheBonus() {
        double mult = CavalryChargePolicy.damageMultiplierFor(
                CavalryChargeState.CHARGING, CombatRole.INFANTRY, false);

        assertEquals(CavalryChargePolicy.FIRST_HIT_BONUS_MULTIPLIER, mult, 1e-9);
    }

    @Test
    void chargingIntoRangedBacklineAlsoTriggersTheBonus() {
        // Ranged units in melee are the second class of "unsupported infantry" the charge
        // is meant to punish.
        double mult = CavalryChargePolicy.damageMultiplierFor(
                CavalryChargeState.CHARGING, CombatRole.RANGED, false);

        assertEquals(CavalryChargePolicy.FIRST_HIT_BONUS_MULTIPLIER, mult, 1e-9);
    }

    @Test
    void chargingIntoBracedPikeReceivesThePenalty() {
        double mult = CavalryChargePolicy.damageMultiplierFor(
                CavalryChargeState.CHARGING, CombatRole.PIKE, true);

        assertEquals(CavalryChargePolicy.PIKE_BRACE_PENALTY_MULTIPLIER, mult, 1e-9);
    }

    @Test
    void unbracedPikeDoesNotPunishTheCharge() {
        // The pike's anti-cavalry penalty requires the brace flag — a pike caught with the
        // weapon down behaves like an INFANTRY target relative to this policy's penalty,
        // but the bonus only applies to the INFANTRY enum, so pike-no-brace is neutral.
        double mult = CavalryChargePolicy.damageMultiplierFor(
                CavalryChargeState.CHARGING, CombatRole.PIKE, false);

        assertEquals(1.0D, mult, 1e-9);
    }

    @Test
    void exhaustedChargeProducesNoBonusEvenAgainstUnbracedInfantry() {
        // Exhaustion is the spam-prevention gate — no bonus, no penalty.
        double mult = CavalryChargePolicy.damageMultiplierFor(
                CavalryChargeState.EXHAUSTED, CombatRole.INFANTRY, false);

        assertEquals(1.0D, mult, 1e-9);
    }

    @Test
    void notChargingTreatsEveryTargetAsBaseline() {
        for (CombatRole role : CombatRole.values()) {
            assertEquals(1.0D,
                    CavalryChargePolicy.damageMultiplierFor(
                            CavalryChargeState.NOT_CHARGING, role, true),
                    1e-9, "NOT_CHARGING vs " + role);
            assertEquals(1.0D,
                    CavalryChargePolicy.damageMultiplierFor(
                            CavalryChargeState.NOT_CHARGING, role, false),
                    1e-9, "NOT_CHARGING vs " + role + " (unbraced)");
        }
    }

    @Test
    void cavalryOnCavalryChargeIsNeutral() {
        // Two heavy horse colliding head-on still hits, but the charge bonus is reserved for
        // the asymmetric infantry / ranged case.
        double mult = CavalryChargePolicy.damageMultiplierFor(
                CavalryChargeState.CHARGING, CombatRole.CAVALRY, false);

        assertEquals(1.0D, mult, 1e-9);
    }

    @Test
    void nullChargeStateOrTargetRoleReturnsBaseline() {
        assertEquals(1.0D, CavalryChargePolicy.damageMultiplierFor(null, CombatRole.INFANTRY, false), 1e-9);
        // null role with CHARGING falls through to the "unhandled role" baseline.
        assertEquals(1.0D, CavalryChargePolicy.damageMultiplierFor(
                CavalryChargeState.CHARGING, null, false), 1e-9);
    }

    @Test
    void chargingWithHitTransitionsToExhausted() {
        assertEquals(CavalryChargeState.EXHAUSTED,
                CavalryChargePolicy.advance(CavalryChargeState.CHARGING, true, 0,
                        CavalryChargePolicy.CHARGE_COOLDOWN_TICKS));
    }

    @Test
    void chargingWithoutHitStaysCharging() {
        assertEquals(CavalryChargeState.CHARGING,
                CavalryChargePolicy.advance(CavalryChargeState.CHARGING, false, 0,
                        CavalryChargePolicy.CHARGE_COOLDOWN_TICKS));
    }

    @Test
    void exhaustedClearsToNotChargingAfterCooldown() {
        assertEquals(CavalryChargeState.EXHAUSTED,
                CavalryChargePolicy.advance(CavalryChargeState.EXHAUSTED, false,
                        CavalryChargePolicy.CHARGE_COOLDOWN_TICKS - 1,
                        CavalryChargePolicy.CHARGE_COOLDOWN_TICKS));

        assertEquals(CavalryChargeState.NOT_CHARGING,
                CavalryChargePolicy.advance(CavalryChargeState.EXHAUSTED, false,
                        CavalryChargePolicy.CHARGE_COOLDOWN_TICKS,
                        CavalryChargePolicy.CHARGE_COOLDOWN_TICKS));
    }

    @Test
    void notChargingStaysNotCharging() {
        assertEquals(CavalryChargeState.NOT_CHARGING,
                CavalryChargePolicy.advance(CavalryChargeState.NOT_CHARGING, true, 0, 60));
    }

    @Test
    void nullStateAdvancesToNotCharging() {
        assertEquals(CavalryChargeState.NOT_CHARGING,
                CavalryChargePolicy.advance(null, true, 100, 60));
    }
}
