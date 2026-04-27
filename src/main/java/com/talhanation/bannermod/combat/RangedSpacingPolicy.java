package com.talhanation.bannermod.combat;

/**
 * Pure spacing decision for a ranged unit. Returns the {@link RangedAction} the upcoming
 * combat-AI hookup should apply on top of the existing movement / aim goal.
 *
 * <p>Three rules, evaluated in order — the first match wins:
 *
 * <ol>
 *   <li>Enemy melee within {@link #ENEMY_FALLBACK_RADIUS} → {@link RangedAction#FALLBACK}.
 *       Implements the "fallback when cavalry/melee breaks through" acceptance criterion.</li>
 *   <li>Own melee line front within {@link #FRIENDLY_BACKLINE_BUFFER} (i.e. friendly line
 *       collapsed onto the ranged unit) → {@link RangedAction#FALLBACK} too. Without this
 *       the ranged unit would happily stand inside its own melee.</li>
 *   <li>{@code firingLaneBlockedByAlly} → {@link RangedAction#LATERAL_SHIFT} so the unit
 *       sidesteps the obstruction while keeping its rear-rank distance.</li>
 * </ol>
 *
 * <p>Otherwise the unit stays. The two distance thresholds and the buffer are exposed as
 * public constants so a future Forge-config layer can override them without touching the
 * decision tree.
 */
public final class RangedSpacingPolicy {

    /**
     * Inside this radius an enemy melee unit is treated as "broken through" and the ranged
     * unit must fall back. 6 blocks ~= one chest's reach for a sword swing.
     */
    public static final double ENEMY_FALLBACK_RADIUS = 6.0D;

    /**
     * Minimum distance the ranged unit wants to keep from its own melee line front. If the
     * line collapses inside this buffer the ranged unit fades back to re-establish spacing.
     */
    public static final double FRIENDLY_BACKLINE_BUFFER = 4.0D;

    private RangedSpacingPolicy() {
    }

    /**
     * @param distanceToNearestEnemyMelee shortest 3D distance to a hostile unit currently in
     *                                    a melee posture; pass {@link Double#POSITIVE_INFINITY}
     *                                    when no enemy is close enough to register.
     * @param distanceToOwnMeleeLineFront shortest 3D distance to a friendly melee unit; pass
     *                                    {@link Double#POSITIVE_INFINITY} when no friendly
     *                                    melee is close enough to constrain spacing.
     * @param firingLaneBlockedByAlly     {@code true} when an ally sits in the unit's firing
     *                                    cone and is occluding the line of sight to the
     *                                    chosen target.
     */
    public static RangedAction decide(double distanceToNearestEnemyMelee,
                                      double distanceToOwnMeleeLineFront,
                                      boolean firingLaneBlockedByAlly) {
        double enemyDistance = sanitize(distanceToNearestEnemyMelee);
        double friendlyDistance = sanitize(distanceToOwnMeleeLineFront);

        if (enemyDistance < ENEMY_FALLBACK_RADIUS) {
            return RangedAction.FALLBACK;
        }
        if (friendlyDistance < FRIENDLY_BACKLINE_BUFFER) {
            return RangedAction.FALLBACK;
        }
        if (firingLaneBlockedByAlly) {
            return RangedAction.LATERAL_SHIFT;
        }
        return RangedAction.STAY;
    }

    private static double sanitize(double value) {
        if (Double.isNaN(value) || value < 0.0D) {
            return Double.POSITIVE_INFINITY;
        }
        return value;
    }
}
