package com.talhanation.bannermod.combat;

/**
 * Three-tier charge state for a cavalry unit.
 *
 * <ul>
 *   <li>{@link #NOT_CHARGING} — unit moves and fights at baseline. No first-hit bonus.</li>
 *   <li>{@link #CHARGING} — unit is mid-charge: a successful hit triggers the first-hit
 *       bonus (or the pike penalty), and the next tick transitions the unit to
 *       {@link #EXHAUSTED} so the bonus cannot be applied twice in a row.</li>
 *   <li>{@link #EXHAUSTED} — unit just finished a charge; charge bonus is disabled until
 *       {@code chargeCooldownTicks} elapse, at which point the unit returns to
 *       {@link #NOT_CHARGING}.</li>
 * </ul>
 */
public enum CavalryChargeState {
    NOT_CHARGING,
    CHARGING,
    EXHAUSTED
}
