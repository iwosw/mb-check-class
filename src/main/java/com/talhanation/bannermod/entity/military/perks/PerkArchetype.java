package com.talhanation.bannermod.entity.military.perks;

/**
 * Archetype slots a perk can belong to. {@link #UNIVERSAL} marks general-stat perks
 * (HP, KB-resist, attack damage / speed, movement, ranged accuracy / velocity) that
 * every recruit archetype can pick up; the remaining values pin a perk to one
 * combat role so SKILLTREE-003 can author per-class trees on top of this model.
 *
 * <p>Resolved server-side from the concrete recruit entity class; never trust a
 * client-supplied archetype value.</p>
 */
public enum PerkArchetype {
    UNIVERSAL,
    SWORDSMAN,
    BOWMAN,
    CROSSBOWMAN,
    PIKEMAN,
    CAVALRY
}
