package com.talhanation.bannermod.ai.military;

import java.util.List;

/**
 * Stage 4.C: brace-for-charge against incoming cavalry.
 *
 * <p>When a hostile mounted entity is close enough, a recruit with a reach weapon
 * or a shield should auto-raise its shield, halt, and brace. The helper here is
 * the pure decision logic; integration callers resolve the world-space inputs
 * into the simple {@link HostileObservation} record.
 *
 * <p>Kept framework-free so unit tests only need plain records and no Minecraft
 * runtime.
 */
public final class BraceAgainstChargePolicy {

    /** Radius (blocks) within which a charging cavalry triggers bracing. */
    public static final double BRACE_RADIUS = 10.0D;
    /** Squared {@link #BRACE_RADIUS}, for fast proximity tests. */
    public static final double BRACE_RADIUS_SQR = BRACE_RADIUS * BRACE_RADIUS;
    /** Extra damage reduction (fraction REMAINING) while bracing a cavalry rider. */
    public static final float BRACE_CAVALRY_REMAINING = 0.70f;

    private BraceAgainstChargePolicy() {
    }

    /**
     * Snapshot of a nearby hostile relevant to bracing.
     *
     * @param distSqr           squared distance from recruit to hostile
     * @param mounted           true if hostile currently has a LivingEntity vehicle
     *                          (or is itself cavalry-like)
     * @param approachingRecruit true if hostile's velocity points toward the recruit;
     *                          pass {@code true} when the caller opts to skip the
     *                          velocity test (per the spec: proximity is enough)
     */
    public record HostileObservation(double distSqr, boolean mounted, boolean approachingRecruit) {
    }

    /**
     * True iff the recruit should brace: stance is non-LOOSE, the recruit is
     * either a shieldman or wielding a reach weapon, and at least one observed
     * hostile is within {@link #BRACE_RADIUS} and mounted.
     *
     * @param stance         recruit's current stance; LOOSE always returns false
     * @param hasShield      true if the recruit can actually raise a shield
     * @param hasReachWeapon true if recruit's main-hand grants extra reach (spear/pike/etc.)
     * @param hostiles       observations of nearby hostile entities (one entry each)
     */
    public static boolean shouldBrace(CombatStance stance,
                                      boolean hasShield,
                                      boolean hasReachWeapon,
                                      List<HostileObservation> hostiles) {
        if (stance == null || stance == CombatStance.LOOSE) {
            return false;
        }
        if (!hasShield && !hasReachWeapon) {
            return false;
        }
        if (hostiles == null || hostiles.isEmpty()) {
            return false;
        }
        for (HostileObservation hostile : hostiles) {
            if (hostile == null) continue;
            if (!hostile.mounted()) continue;
            if (hostile.distSqr() > BRACE_RADIUS_SQR) continue;
            if (!hostile.approachingRecruit()) continue;
            return true;
        }
        return false;
    }
}
