package com.talhanation.bannermod.combat;

/**
 * Pure input record for {@link MoralePolicy#evaluate(MoraleSnapshot)}.
 *
 * <p>Counts and flags only — no entity references — so the policy stays unit-testable without
 * a live world. Production-side wiring (later slice) will populate this record from a recruit's
 * formation neighbourhood / target tracker / commander aura.
 *
 * @param squadSize          original squad headcount before any casualties; clamped to >= 1.
 * @param casualtiesTaken    dead or severely-wounded since the engagement started; clamped to
 *                           [0, {@code squadSize}].
 * @param hostileVisibleSize enemy actor count the squad can see; clamped to >= 0.
 * @param commanderPresent   {@code true} if a captain/commander/leader is alive within the
 *                           configured aura radius. Used to downgrade ROUTED -> SHAKEN.
 * @param nearbyAllyCount    same-side actors near the squad excluding the squad itself; used
 *                           to gate the {@link #isolated()} flag's penalty contribution.
 * @param recentDamageEvents distinct damage-source events the squad absorbed in the trailing
 *                           sustained-fire window; used by the suppression check.
 * @param isolated           {@code true} if {@code nearbyAllyCount == 0} OR if the squad is
 *                           outside its formation cohesion radius. Pre-computed by the caller
 *                           so the policy stays a pure function.
 */
public record MoraleSnapshot(
        int squadSize,
        int casualtiesTaken,
        int hostileVisibleSize,
        boolean commanderPresent,
        int nearbyAllyCount,
        int recentDamageEvents,
        boolean isolated
) {
    public MoraleSnapshot {
        squadSize = Math.max(1, squadSize);
        casualtiesTaken = Math.max(0, Math.min(squadSize, casualtiesTaken));
        hostileVisibleSize = Math.max(0, hostileVisibleSize);
        nearbyAllyCount = Math.max(0, nearbyAllyCount);
        recentDamageEvents = Math.max(0, recentDamageEvents);
    }

    /** Survivors at the moment of the snapshot. */
    public int survivors() {
        return Math.max(0, squadSize - casualtiesTaken);
    }

    /** Casualty fraction in [0.0, 1.0]. */
    public double casualtyRatio() {
        return (double) casualtiesTaken / (double) squadSize;
    }
}
