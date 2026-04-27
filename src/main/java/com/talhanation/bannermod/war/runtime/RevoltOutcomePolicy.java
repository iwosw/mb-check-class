package com.talhanation.bannermod.war.runtime;

/**
 * Pure-logic resolution of an objective-controlled revolt.
 *
 * <p>The policy rejects timer-only auto-success: an empty objective leaves the revolt
 * {@link RevoltState#PENDING} so it can be re-evaluated on the next battle window instead of
 * silently flipping ownership while no one is contesting. A rebel presence with zero defenders
 * succeeds; any defender presence fails the revolt regardless of how many rebels showed up
 * (defender-holds-the-objective tiebreak).
 */
public final class RevoltOutcomePolicy {
    private RevoltOutcomePolicy() {
    }

    public static RevoltState evaluate(int rebelPresenceCount, int occupierPresenceCount) {
        int rebels = Math.max(0, rebelPresenceCount);
        int occupiers = Math.max(0, occupierPresenceCount);
        if (rebels == 0 && occupiers == 0) {
            return RevoltState.PENDING;
        }
        if (occupiers > 0) {
            return RevoltState.FAILED;
        }
        return RevoltState.SUCCESS;
    }
}
