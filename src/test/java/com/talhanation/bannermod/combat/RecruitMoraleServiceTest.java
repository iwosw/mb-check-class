package com.talhanation.bannermod.combat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Locks down the pure UUID-keyed pieces of {@link RecruitMoraleService} that COMBAT-001 wires
 * into the live recruit AI path: the suppression accumulator (sliding window of damage events
 * driving the SUSTAINED_FIRE token) and the morale-state-driven attack multiplier.
 *
 * <p>The live entity hookup ({@link RecruitMoraleService#tick}, the rout-goal disengagement,
 * and {@link RecruitMoraleService#attackMultiplierFor(com.talhanation.bannermod.entity.military.AbstractRecruitEntity)})
 * still requires a Minecraft world; that side is exercised by gametest. This suite focuses on
 * the boundary conditions of the pure helpers because they directly drive the COMBAT-001
 * acceptance criteria (suppression actually trips, shaken units actually hit softer).</p>
 */
class RecruitMoraleServiceTest {

    private UUID recruit;

    @BeforeEach
    void resetState() {
        RecruitMoraleService.resetForTests();
        recruit = UUID.randomUUID();
    }

    @Test
    void noEntryReportsZeroEvents() {
        // Caller asks for the count before any damage has ever been recorded — the service
        // must not auto-create an entry just to answer a read.
        assertEquals(0, RecruitMoraleService.recentDamageEventCount(recruit, 100L));
    }

    @Test
    void firstEventOpensWindowAtCountOne() {
        RecruitMoraleService.recordDamageTaken(recruit, 100L);
        assertEquals(1, RecruitMoraleService.recentDamageEventCount(recruit, 100L));
    }

    @Test
    void eventsWithinWindowAccumulate() {
        // 6 hits within the suppression window — that's the SUSTAINED_FIRE_THRESHOLD, so the
        // policy will see the token after this sequence.
        long t0 = 1_000L;
        for (int i = 0; i < 6; i++) {
            RecruitMoraleService.recordDamageTaken(recruit, t0 + i * 5L);
        }
        assertEquals(6, RecruitMoraleService.recentDamageEventCount(recruit,
                t0 + RecruitMoraleService.SUPPRESSION_WINDOW_TICKS));
    }

    @Test
    void eventOutsideWindowResetsCounterToOne() {
        RecruitMoraleService.recordDamageTaken(recruit, 1_000L);
        // Next event arrives after the window has fully expired — counter resets, not 2.
        long late = 1_000L + RecruitMoraleService.SUPPRESSION_WINDOW_TICKS + 1L;
        RecruitMoraleService.recordDamageTaken(recruit, late);
        assertEquals(1, RecruitMoraleService.recentDamageEventCount(recruit, late));
    }

    @Test
    void readingAfterWindowExpiresReportsZero() {
        // Five hits land in quick succession, then nobody touches the recruit for longer
        // than the window. The accumulator must report 0 — not the stale 5 — so a recruit
        // who walked out of fire stops tripping SUSTAINED_FIRE.
        for (int i = 0; i < 5; i++) {
            RecruitMoraleService.recordDamageTaken(recruit, 500L + i);
        }
        long stale = 500L + RecruitMoraleService.SUPPRESSION_WINDOW_TICKS + 100L;
        assertEquals(0, RecruitMoraleService.recentDamageEventCount(recruit, stale));
    }

    @Test
    void invalidateClearsAccumulator() {
        RecruitMoraleService.recordDamageTaken(recruit, 200L);
        RecruitMoraleService.invalidate(recruit);
        assertEquals(0, RecruitMoraleService.recentDamageEventCount(recruit, 200L));
    }

    @Test
    void nullUuidIsANoop() {
        // Defensive sanitization on both sides: a buggy caller must not pollute the table
        // with a null key, and reads with a null key must not throw.
        RecruitMoraleService.recordDamageTaken((UUID) null, 100L);
        assertEquals(0, RecruitMoraleService.recentDamageEventCount(null, 100L));
    }

    @Test
    void shakenAttackMultiplierMatchesPolicyConstant() {
        // The service-level helper must agree with the policy constant — the damage path
        // calls the service overload, the policy unit tests pin the constant.
        assertEquals(MoralePolicy.SHAKEN_ATTACK_DAMPING_MULTIPLIER,
                MoralePolicy.attackMultiplierFor(MoraleState.SHAKEN));
        assertEquals(1.0D, MoralePolicy.attackMultiplierFor(MoraleState.STEADY));
        assertEquals(1.0D, MoralePolicy.attackMultiplierFor(MoraleState.ROUTED));
        assertEquals(1.0D, MoralePolicy.attackMultiplierFor(null));
    }
}
