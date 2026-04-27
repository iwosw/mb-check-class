package com.talhanation.bannermod.combat;

/**
 * Three-tier movement intent for a ranged unit, decided by
 * {@link RangedSpacingPolicy#decide(double, double, boolean)}.
 *
 * <ul>
 *   <li>{@link #STAY} — current position is good for shooting; hold and fire.</li>
 *   <li>{@link #FALLBACK} — enemy melee broke through or own melee line collapsed onto the
 *       backline; retreat directly away from the threat.</li>
 *   <li>{@link #LATERAL_SHIFT} — distance is fine but a friendly mob is in the firing lane;
 *       sidestep to clear the lane while preserving the rear-rank position.</li>
 * </ul>
 */
public enum RangedAction {
    STAY,
    FALLBACK,
    LATERAL_SHIFT
}
