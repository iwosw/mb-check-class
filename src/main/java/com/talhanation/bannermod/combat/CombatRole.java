package com.talhanation.bannermod.combat;

/**
 * Coarse role classification a target advertises to the combat policies. Only the
 * distinctions that matter for the cavalry/pike interaction are modelled here; the existing
 * recruit-class enums stay authoritative for everything else.
 */
public enum CombatRole {
    /** Generic infantry: spearmen without a planted brace, melee, swordsmen, axemen. */
    INFANTRY,
    /** Pike / spear-wall variant. Only contributes the anti-cavalry penalty when braced. */
    PIKE,
    /** Mounted units: horsemen, cataphracts. */
    CAVALRY,
    /** Ranged backline: bowmen, crossbowmen. */
    RANGED
}
