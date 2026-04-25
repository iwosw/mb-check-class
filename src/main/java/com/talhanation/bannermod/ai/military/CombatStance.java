package com.talhanation.bannermod.ai.military;

import java.util.Locale;

/**
 * Stage 1 combat stance for squads/recruits.
 *
 * <p>Controls how far a recruit is allowed to drift from its formation slot while
 * chasing a combat target, and (in later stages) how tightly they cohere in yaw
 * and micro-behaviours like auto-block.
 *
 * <p>Stages 2+ will add extra stances and per-stance behaviours; the enum is
 * intentionally minimal so that callers and persisted data stay stable.
 */
public enum CombatStance {
    /** Current (pre-Stage-1) behaviour. Default for existing saves. */
    LOOSE,
    /** Recruit may leave its slot up to a short leash. */
    LINE_HOLD,
    /** Tighter leash and extra cohesion. Stage 2 will add auto-block here. */
    SHIELD_WALL;

    public static CombatStance fromName(String name) {
        if (name == null || name.isBlank()) {
            return LOOSE;
        }
        try {
            return CombatStance.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return LOOSE;
        }
    }
}
