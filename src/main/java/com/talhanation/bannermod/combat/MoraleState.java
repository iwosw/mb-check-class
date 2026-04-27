package com.talhanation.bannermod.combat;

/**
 * Three-tier squad morale state used by {@link MoralePolicy}.
 *
 * <ul>
 *   <li>{@link #STEADY} — squad fights normally.</li>
 *   <li>{@link #SHAKEN} — squad still engages but at reduced effectiveness; reserved for the
 *       upcoming combat-math hookup (e.g. damage / hit-rate dampening).</li>
 *   <li>{@link #ROUTED} — squad disengages and flees; AI integration is a follow-up slice.</li>
 * </ul>
 */
public enum MoraleState {
    STEADY,
    SHAKEN,
    ROUTED
}
