package com.talhanation.bannermod.war.cooldown;

/**
 * Discriminator for {@code WarCooldownRecord} entries. Each (entity, kind) pair holds at
 * most one active cooldown — granting a new one with the same kind replaces the existing
 * record's expiry rather than stacking entries.
 */
public enum WarCooldownKind {
    /** Defender just lost territory; nobody can re-declare against them for a while. */
    LOST_TERRITORY_IMMUNITY,
    /** Recent PEACEFUL status toggle; further toggles are blocked until the timer elapses. */
    PEACEFUL_TOGGLE_RECENT;

    public static WarCooldownKind fromTagName(String name) {
        if (name == null || name.isBlank()) {
            return LOST_TERRITORY_IMMUNITY;
        }
        try {
            return WarCooldownKind.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return LOST_TERRITORY_IMMUNITY;
        }
    }
}
