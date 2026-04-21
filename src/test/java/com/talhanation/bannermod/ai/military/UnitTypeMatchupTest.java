package com.talhanation.bannermod.ai.military;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Stage 4.D: HYW-style unit-type counter matrix. */
class UnitTypeMatchupTest {

    private static final double EPS = 1e-6;

    // ---- classify() ---------------------------------------------------------

    @Test
    void mountedBecomesCavalry() {
        assertEquals(UnitTypeMatchup.UnitClass.CAVALRY,
                UnitTypeMatchup.classify(true, 0.0, false, false, 0));
    }

    @Test
    void pikeReachBecomesPikeInfantry() {
        assertEquals(UnitTypeMatchup.UnitClass.PIKE_INFANTRY,
                UnitTypeMatchup.classify(false, WeaponReach.PIKE_EXTRA_REACH, false, false, 0));
        assertEquals(UnitTypeMatchup.UnitClass.PIKE_INFANTRY,
                UnitTypeMatchup.classify(false, WeaponReach.SPEAR_EXTRA_REACH, false, false, 0));
    }

    @Test
    void rangedWeaponBecomesRanged() {
        assertEquals(UnitTypeMatchup.UnitClass.RANGED,
                UnitTypeMatchup.classify(false, 0.0, true, false, 0));
    }

    @Test
    void shieldmanIsHeavy() {
        assertEquals(UnitTypeMatchup.UnitClass.HEAVY,
                UnitTypeMatchup.classify(false, 0.0, false, true, 0));
    }

    @Test
    void plateChestArmorIsHeavy() {
        assertEquals(UnitTypeMatchup.UnitClass.HEAVY,
                UnitTypeMatchup.classify(false, 0.0, false, false,
                        UnitTypeMatchup.HEAVY_PLATE_DEFENSE_THRESHOLD));
    }

    @Test
    void lightIsDefaultFallback() {
        assertEquals(UnitTypeMatchup.UnitClass.LIGHT,
                UnitTypeMatchup.classify(false, 0.0, false, false, 0));
    }

    @Test
    void mountedOutranksOtherFlags() {
        // Mounted recruit with a bow is still CAVALRY.
        assertEquals(UnitTypeMatchup.UnitClass.CAVALRY,
                UnitTypeMatchup.classify(true, 0.0, true, true, 8));
    }

    // ---- damageMultiplier() -------------------------------------------------

    @Test
    void lightVsHeavyBouncesOff() {
        assertEquals(0.80D,
                UnitTypeMatchup.damageMultiplier(UnitTypeMatchup.UnitClass.LIGHT, UnitTypeMatchup.UnitClass.HEAVY), EPS);
    }

    @Test
    void heavyVsLightHitsHarder() {
        assertEquals(1.20D,
                UnitTypeMatchup.damageMultiplier(UnitTypeMatchup.UnitClass.HEAVY, UnitTypeMatchup.UnitClass.LIGHT), EPS);
    }

    @Test
    void cavalryVsLightOrRangedCharges() {
        assertEquals(1.40D,
                UnitTypeMatchup.damageMultiplier(UnitTypeMatchup.UnitClass.CAVALRY, UnitTypeMatchup.UnitClass.LIGHT), EPS);
        assertEquals(1.40D,
                UnitTypeMatchup.damageMultiplier(UnitTypeMatchup.UnitClass.CAVALRY, UnitTypeMatchup.UnitClass.RANGED), EPS);
    }

    @Test
    void lightOrRangedFootVsCavalryStruggle() {
        assertEquals(0.90D,
                UnitTypeMatchup.damageMultiplier(UnitTypeMatchup.UnitClass.LIGHT, UnitTypeMatchup.UnitClass.CAVALRY), EPS);
        assertEquals(0.90D,
                UnitTypeMatchup.damageMultiplier(UnitTypeMatchup.UnitClass.RANGED, UnitTypeMatchup.UnitClass.CAVALRY), EPS);
    }

    @Test
    void pikeCountersCavalry() {
        assertEquals(1.50D,
                UnitTypeMatchup.damageMultiplier(UnitTypeMatchup.UnitClass.PIKE_INFANTRY, UnitTypeMatchup.UnitClass.CAVALRY), EPS);
    }

    @Test
    void rangedVsRangedIsNeutral() {
        assertEquals(1.0D,
                UnitTypeMatchup.damageMultiplier(UnitTypeMatchup.UnitClass.RANGED, UnitTypeMatchup.UnitClass.RANGED), EPS);
    }

    @Test
    void unrelatedMatchupsAreNeutral() {
        assertEquals(1.0D,
                UnitTypeMatchup.damageMultiplier(UnitTypeMatchup.UnitClass.HEAVY, UnitTypeMatchup.UnitClass.HEAVY), EPS);
        assertEquals(1.0D,
                UnitTypeMatchup.damageMultiplier(UnitTypeMatchup.UnitClass.PIKE_INFANTRY, UnitTypeMatchup.UnitClass.LIGHT), EPS);
        assertEquals(1.0D,
                UnitTypeMatchup.damageMultiplier(UnitTypeMatchup.UnitClass.CAVALRY, UnitTypeMatchup.UnitClass.HEAVY), EPS);
    }

    @Test
    void nullInputsAreNeutral() {
        assertEquals(1.0D,
                UnitTypeMatchup.damageMultiplier(null, UnitTypeMatchup.UnitClass.LIGHT), EPS);
        assertEquals(1.0D,
                UnitTypeMatchup.damageMultiplier(UnitTypeMatchup.UnitClass.LIGHT, null), EPS);
        assertEquals(1.0D, UnitTypeMatchup.damageMultiplier(null, null), EPS);
    }
}
