package com.talhanation.bannermod.army.command;

/**
 * Flat verb enum mirroring the {@link CommandIntent} variants. Useful for logging,
 * metrics, and persistent identifiers that need a stable primitive handle.
 */
public enum CommandIntentType {
    MOVEMENT,
    FACE,
    ATTACK,
    STRATEGIC_FIRE,
    AGGRO,
    COMBAT_STANCE,
    SIEGE_MACHINE
}
