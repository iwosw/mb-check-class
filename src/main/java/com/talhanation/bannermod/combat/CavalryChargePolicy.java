package com.talhanation.bannermod.combat;

/**
 * Pure interaction math for a cavalry strike. Given the charging unit's
 * {@link CavalryChargeState} and the target's {@link CombatRole} / brace flag, returns the
 * damage modifier the combat-AI hookup should apply on top of baseline melee damage.
 *
 * <p>Three observable behaviours, matching the COMBAT-004 acceptance criteria:
 *
 * <ul>
 *   <li>{@link #FIRST_HIT_BONUS_MULTIPLIER} on the first {@link CavalryChargeState#CHARGING}
 *       hit when the target is unbraced infantry / ranged ("Cavalry charge punishes
 *       unsupported infantry").</li>
 *   <li>{@link #PIKE_BRACE_PENALTY_MULTIPLIER} when the cavalry charges into a braced pike
 *       ("Pike line punishes frontal cavalry charge"). The penalty supersedes the bonus.</li>
 *   <li>Charges fired while {@link CavalryChargeState#EXHAUSTED} get {@code 1.0}
 *       (no bonus, no penalty) regardless of target — the {@code EXHAUSTED} state is the
 *       gate that "Exhaustion prevents charge spam".</li>
 * </ul>
 *
 * <p>Tunable multipliers and the cooldown duration are public constants so a future
 * Forge-config layer can override them without changing the decision tree.
 */
public final class CavalryChargePolicy {

    /** Multiplier applied to the first {@link CavalryChargeState#CHARGING} melee hit vs unbraced infantry / ranged. */
    public static final double FIRST_HIT_BONUS_MULTIPLIER = 2.0D;

    /** Multiplier applied to a charging strike that lands on a braced pike. */
    public static final double PIKE_BRACE_PENALTY_MULTIPLIER = 0.25D;

    /** Default cooldown ticks between charges (60 ticks = 3 s on a 20 TPS server). */
    public static final int CHARGE_COOLDOWN_TICKS = 60;

    private CavalryChargePolicy() {
    }

    /**
     * @param chargeState     attacker's current charge state.
     * @param targetRole      target's combat role.
     * @param targetIsBraced  {@code true} if the target advertised "held ground / brace
     *                        for charge". Only meaningful for {@link CombatRole#PIKE}.
     * @return damage multiplier to apply on top of baseline melee. {@code 1.0} = no
     *         modification; {@code > 1.0} = bonus; {@code < 1.0} = penalty.
     */
    public static double damageMultiplierFor(CavalryChargeState chargeState,
                                             CombatRole targetRole,
                                             boolean targetIsBraced) {
        if (chargeState == null || chargeState == CavalryChargeState.NOT_CHARGING) {
            return 1.0D;
        }
        if (chargeState == CavalryChargeState.EXHAUSTED) {
            // Charge cooldown gate: spam-charging produces no bonus and no penalty.
            return 1.0D;
        }
        // CHARGING.
        if (targetRole == CombatRole.PIKE && targetIsBraced) {
            return PIKE_BRACE_PENALTY_MULTIPLIER;
        }
        if (targetRole == CombatRole.INFANTRY || targetRole == CombatRole.RANGED) {
            return FIRST_HIT_BONUS_MULTIPLIER;
        }
        // CAVALRY-on-CAVALRY or unbraced PIKE: charge connects but the bonus does not apply.
        return 1.0D;
    }

    /**
     * Pure state-machine advance for the charge tick lifecycle. The combat-AI hookup will
     * call this once per tick after damage resolution to decide the next state.
     *
     * @param current             current charge state.
     * @param hitConnected        {@code true} if the charging unit landed a melee strike on
     *                            this tick. Used to transition CHARGING -> EXHAUSTED.
     * @param ticksSinceExhausted ticks elapsed since entering EXHAUSTED. Ignored when
     *                            {@code current != EXHAUSTED}. Used to transition
     *                            EXHAUSTED -> NOT_CHARGING after the cooldown.
     * @param cooldownTicks       cooldown duration in ticks. Pass
     *                            {@link #CHARGE_COOLDOWN_TICKS} in production.
     * @return next state for the unit.
     */
    public static CavalryChargeState advance(CavalryChargeState current,
                                             boolean hitConnected,
                                             int ticksSinceExhausted,
                                             int cooldownTicks) {
        if (current == null) {
            return CavalryChargeState.NOT_CHARGING;
        }
        return switch (current) {
            case NOT_CHARGING -> CavalryChargeState.NOT_CHARGING;
            case CHARGING -> hitConnected ? CavalryChargeState.EXHAUSTED : CavalryChargeState.CHARGING;
            case EXHAUSTED -> {
                int cooldown = Math.max(0, cooldownTicks);
                int elapsed = Math.max(0, ticksSinceExhausted);
                yield elapsed >= cooldown ? CavalryChargeState.NOT_CHARGING : CavalryChargeState.EXHAUSTED;
            }
        };
    }
}
