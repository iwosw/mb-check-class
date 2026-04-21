package com.talhanation.bannermod.ai.military;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Stage 4.A: flank damage multipliers. */
class FlankDamageTest {

    private static final float EPS = 1e-4f;

    @Test
    void frontHitHasNoBonus() {
        assertEquals(1.0f, FlankDamage.multiplierFor(FacingHitZone.FRONT), EPS);
    }

    @Test
    void sideHitIsFifteenPercentBonus() {
        assertEquals(1.15f, FlankDamage.multiplierFor(FacingHitZone.SIDE), EPS);
    }

    @Test
    void backHitIsFiftyPercentBonus() {
        assertEquals(1.5f, FlankDamage.multiplierFor(FacingHitZone.BACK), EPS);
    }

    @Test
    void nullZoneFallsBackToFront() {
        assertEquals(1.0f, FlankDamage.multiplierFor(null), EPS);
    }
}
