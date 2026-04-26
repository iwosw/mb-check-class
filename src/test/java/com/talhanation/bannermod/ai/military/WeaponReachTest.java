package com.talhanation.bannermod.ai.military;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Stage 3.A: per-item reach tag — pure id heuristics.
 */
class WeaponReachTest {

    private static final double EPS = 1e-6D;

    @Test
    void plainMeleeGrantsZeroExtraReach() {
        assertEquals(0.0D, WeaponReach.effectiveReachForId("minecraft:iron_sword"), EPS);
        assertEquals(0.0D, WeaponReach.effectiveReachForId("minecraft:iron_axe"), EPS);
        assertEquals(0.0D, WeaponReach.effectiveReachForId("bannermod:something_else"), EPS);
    }

    @Test
    void spearGrantsOneBlockExtraReach() {
        assertEquals(WeaponReach.SPEAR_EXTRA_REACH,
                WeaponReach.effectiveReachForId("recruits:iron_spear"), EPS);
        assertEquals(WeaponReach.SPEAR_EXTRA_REACH,
                WeaponReach.effectiveReachForId("item.bannermod.bronze_spear"), EPS);
    }

    @Test
    void pikeAndHalberdGrantTwoBlockExtraReach() {
        assertEquals(WeaponReach.PIKE_EXTRA_REACH,
                WeaponReach.effectiveReachForId("recruits:steel_pike"), EPS);
        assertEquals(WeaponReach.PIKE_EXTRA_REACH,
                WeaponReach.effectiveReachForId("recruits:halberd"), EPS);
        assertEquals(WeaponReach.PIKE_EXTRA_REACH,
                WeaponReach.effectiveReachForId("recruits:oak_polearm"), EPS);
    }

    @Test
    void polearmPackAliasesGrantTwoBlockExtraReach() {
        assertEquals(WeaponReach.PIKE_EXTRA_REACH,
                WeaponReach.effectiveReachForId("bettercombat:iron_lance"), EPS);
        assertEquals(WeaponReach.PIKE_EXTRA_REACH,
                WeaponReach.effectiveReachForId("medieval:glaive"), EPS);
        assertEquals(WeaponReach.PIKE_EXTRA_REACH,
                WeaponReach.effectiveReachForId("weapons:partisan"), EPS);
        assertEquals(WeaponReach.PIKE_EXTRA_REACH,
                WeaponReach.effectiveReachForId("weapons:billhook"), EPS);
        assertEquals(WeaponReach.PIKE_EXTRA_REACH,
                WeaponReach.effectiveReachForId("weapons:bardiche"), EPS);
        assertEquals(WeaponReach.PIKE_EXTRA_REACH,
                WeaponReach.effectiveReachForId("weapons:voulge"), EPS);
    }

    @Test
    void sarissaAndLongSpearGrantTwoAndAHalfBlockExtraReach() {
        assertEquals(WeaponReach.SARISSA_EXTRA_REACH,
                WeaponReach.effectiveReachForId("recruits:sarissa"), EPS);
        assertEquals(WeaponReach.SARISSA_EXTRA_REACH,
                WeaponReach.effectiveReachForId("recruits:long_spear"), EPS);
        assertEquals(WeaponReach.SARISSA_EXTRA_REACH,
                WeaponReach.effectiveReachForId("recruits:longspear"), EPS);
    }

    @Test
    void sarissaPrecedenceDoesNotCollideWithSpearRule() {
        // "long_spear" must NOT fall through to the spear rule (+1); it is +2.5.
        double reach = WeaponReach.effectiveReachForId("recruits:long_spear");
        assertEquals(WeaponReach.SARISSA_EXTRA_REACH, reach, EPS);
    }

    @Test
    void nullOrEmptyIdReturnsZero() {
        assertEquals(0.0D, WeaponReach.effectiveReachForId(null), EPS);
        assertEquals(0.0D, WeaponReach.effectiveReachForId(""), EPS);
    }

    @Test
    void matchingIsCaseInsensitive() {
        assertEquals(WeaponReach.SPEAR_EXTRA_REACH,
                WeaponReach.effectiveReachForId("RECRUITS:IRON_SPEAR"), EPS);
        assertEquals(WeaponReach.PIKE_EXTRA_REACH,
                WeaponReach.effectiveReachForId("Recruits:Halberd"), EPS);
    }
}
