package com.talhanation.bannermod.entity.military.perks;

/**
 * Stat dimensions a perk bonus can target. Phase 1 (SKILLTREE-002) only stores
 * the bonus payload; the actual attribute / damage / accuracy wiring lands in
 * SKILLTREE-003 (recruits) and SKILLTREE-004 (player) so this enum is the
 * shared vocabulary both sides agree on.
 */
public enum PerkStat {
    /** Vanilla {@code Attributes.MAX_HEALTH}. Stored as flat hit-points. */
    MAX_HEALTH,
    /** Vanilla {@code Attributes.KNOCKBACK_RESISTANCE}. Stored as 0..1 fraction. */
    KNOCKBACK_RESIST,
    /** Vanilla {@code Attributes.ATTACK_DAMAGE}. Stored as flat melee damage. */
    ATTACK_DAMAGE,
    /** Vanilla {@code Attributes.ATTACK_SPEED}. Stored as attacks-per-second delta. */
    ATTACK_SPEED,
    /** Vanilla {@code Attributes.MOVEMENT_SPEED}. Stored as flat speed delta. */
    MOVEMENT_SPEED,
    /** Mod-specific scalar: lower means tighter ranged spread. Stored as fractional delta. */
    RANGED_ACCURACY,
    /** Mod-specific scalar: projectile launch velocity multiplier delta. */
    RANGED_VELOCITY
}
