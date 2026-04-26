package com.talhanation.bannermod.ai.military;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Stage 3.D: per-weapon cadence. Cooldown = baseline * mult + windup.
 */
class AttackCadenceTest {

    /** Representative baseline from {@code AttackUtil.getAttackCooldown}: 20/1.0 + 7 = 27. */
    private static final int BASELINE = 27;

    @Test
    void plainMeleeReturnsBaselineUnchanged() {
        assertEquals(BASELINE, AttackCadence.cooldownTicksFor(BASELINE, "minecraft:iron_sword"));
        assertEquals(BASELINE, AttackCadence.cooldownTicksFor(BASELINE, "minecraft:iron_axe"));
        assertEquals(BASELINE, AttackCadence.cooldownTicksFor(BASELINE, null));
        assertEquals(BASELINE, AttackCadence.cooldownTicksFor(BASELINE, ""));
    }

    @Test
    void spearAddsTwoTicksWindup() {
        int got = AttackCadence.cooldownTicksFor(BASELINE, "recruits:iron_spear");
        assertEquals(BASELINE + AttackCadence.SPEAR_WINDUP_TICKS, got);
        assertEquals(AttackCadence.SPEAR_WINDUP_TICKS,
                AttackCadence.windupTicksFor("recruits:iron_spear"));
    }

    @Test
    void pikeMultipliesBaselineAndAddsFourTicksWindup() {
        int got = AttackCadence.cooldownTicksFor(BASELINE, "recruits:steel_pike");
        int expected = (int) Math.round(BASELINE * AttackCadence.PIKE_COOLDOWN_MULT)
                + AttackCadence.PIKE_WINDUP_TICKS;
        assertEquals(expected, got);
        assertTrue(got > BASELINE + AttackCadence.SPEAR_WINDUP_TICKS,
                "pike cadence should be strictly slower than spear cadence");
    }

    @Test
    void halberdUsesPikeCadence() {
        int got = AttackCadence.cooldownTicksFor(BASELINE, "recruits:halberd");
        int expected = (int) Math.round(BASELINE * AttackCadence.PIKE_COOLDOWN_MULT)
                + AttackCadence.PIKE_WINDUP_TICKS;
        assertEquals(expected, got);
    }

    @Test
    void polearmPackAliasesUsePikeCadence() {
        int expected = (int) Math.round(BASELINE * AttackCadence.PIKE_COOLDOWN_MULT)
                + AttackCadence.PIKE_WINDUP_TICKS;
        assertEquals(expected, AttackCadence.cooldownTicksFor(BASELINE, "bettercombat:iron_lance"));
        assertEquals(expected, AttackCadence.cooldownTicksFor(BASELINE, "medieval:glaive"));
        assertEquals(AttackCadence.PIKE_WINDUP_TICKS,
                AttackCadence.windupTicksFor("weapons:partisan"));
    }

    @Test
    void sarissaUsesSarissaCadence() {
        int got = AttackCadence.cooldownTicksFor(BASELINE, "recruits:sarissa");
        int expected = (int) Math.round(BASELINE * AttackCadence.SARISSA_COOLDOWN_MULT)
                + AttackCadence.SARISSA_WINDUP_TICKS;
        assertEquals(expected, got);
        int pike = AttackCadence.cooldownTicksFor(BASELINE, "recruits:halberd");
        assertTrue(got > pike, "sarissa cadence should be strictly slower than pike");
    }

    @Test
    void longSpearResolvesAsSarissa() {
        assertEquals(AttackCadence.SARISSA_WINDUP_TICKS,
                AttackCadence.windupTicksFor("recruits:long_spear"));
    }

    @Test
    void windupBreakdownMirrorsCadenceBuckets() {
        assertEquals(0, AttackCadence.windupTicksFor("minecraft:iron_sword"));
        assertEquals(AttackCadence.SPEAR_WINDUP_TICKS,
                AttackCadence.windupTicksFor("recruits:spear"));
        assertEquals(AttackCadence.PIKE_WINDUP_TICKS,
                AttackCadence.windupTicksFor("recruits:pike"));
        assertEquals(AttackCadence.SARISSA_WINDUP_TICKS,
                AttackCadence.windupTicksFor("recruits:sarissa"));
    }
}
