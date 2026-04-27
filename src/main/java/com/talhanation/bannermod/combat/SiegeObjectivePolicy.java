package com.talhanation.bannermod.combat;

import java.util.UUID;

/**
 * Pure decisions for siege-objective interactions: who can attack a siege standard, who
 * should escort one, and how a damage event modifies the standard's control pool.
 *
 * <p>The combat-AI hookup will consume:
 *
 * <ul>
 *   <li>{@link #canAttackStandard(UUID, UUID)} from the recruit target-acquisition path
 *       (only opposing-side actors target the standard).</li>
 *   <li>{@link #shouldEscort(UUID, UUID)} from the same-side defend goal (escort own
 *       standards, ignore enemy ones).</li>
 *   <li>{@link #applyDamage(int, int, int)} from the standard block damage handler to
 *       update the control pool deterministically; the {@link DamageOutcome} return tells
 *       the audit log whether this hit destroyed the standard.</li>
 * </ul>
 */
public final class SiegeObjectivePolicy {

    /**
     * Default control-pool capacity for a freshly placed siege standard. Tunable via
     * Forge-config in the future; the default is large enough that a single recruit cannot
     * destroy a standard alone but a coordinated assault can chew through it within the
     * battle window.
     */
    public static final int DEFAULT_CONTROL_POOL = 100;

    private SiegeObjectivePolicy() {
    }

    /**
     * @param attackerPoliticalEntityId attacker's political affiliation; {@code null} means
     *                                  unaffiliated and is never authorised to attack.
     * @param standardPoliticalEntityId political entity that owns the standard; never
     *                                  {@code null} for valid placements.
     * @return {@code true} if the attacker may engage the standard. Same-side actors and
     *         unaffiliated actors cannot.
     */
    public static boolean canAttackStandard(UUID attackerPoliticalEntityId,
                                            UUID standardPoliticalEntityId) {
        if (attackerPoliticalEntityId == null || standardPoliticalEntityId == null) {
            return false;
        }
        return !attackerPoliticalEntityId.equals(standardPoliticalEntityId);
    }

    /**
     * @return {@code true} if the actor is a same-side ally and should escort / defend
     *         the standard. Both ids must be non-null and equal.
     */
    public static boolean shouldEscort(UUID actorPoliticalEntityId,
                                       UUID standardPoliticalEntityId) {
        if (actorPoliticalEntityId == null || standardPoliticalEntityId == null) {
            return false;
        }
        return actorPoliticalEntityId.equals(standardPoliticalEntityId);
    }

    /**
     * Pure control-pool update for a single damage event.
     *
     * @param currentControl current control pool value; clamped to
     *                       [0, {@code maxControl}].
     * @param damage         damage to apply this tick; clamped to {@code >= 0}.
     * @param maxControl     pool capacity; clamped to {@code >= 1}. Used to interpret the
     *                       initial-state guard so a caller can't pass garbage.
     * @return immutable {@link DamageOutcome} with the new control value and a destroyed
     *         flag set when the pool reaches zero on this hit (and was strictly positive
     *         before).
     */
    public static DamageOutcome applyDamage(int currentControl, int damage, int maxControl) {
        int cap = Math.max(1, maxControl);
        int before = Math.max(0, Math.min(cap, currentControl));
        int hit = Math.max(0, damage);
        int after = Math.max(0, before - hit);
        boolean destroyed = before > 0 && after == 0;
        return new DamageOutcome(after, destroyed);
    }

    /**
     * Result of a {@link #applyDamage(int, int, int)} call. {@code destroyed} is
     * {@code true} only when this hit reduced the pool from positive to zero — not when the
     * standard was already at zero and ate another hit, so the caller can write exactly one
     * {@code SIEGE_STANDARD_DESTROYED} audit row regardless of how many cleanup ticks pass.
     */
    public record DamageOutcome(int controlAfter, boolean destroyed) {
    }
}
