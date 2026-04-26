package com.talhanation.bannermod.war.registry;

/**
 * Regulated warfare-RP political status. Higher-level war rules consume this
 * status instead of inferring permissions directly from teams or claims.
 */
public enum PoliticalEntityStatus {
    SETTLEMENT,
    STATE,
    VASSAL,
    PEACEFUL;

    public boolean canDeclareOffensiveWar() {
        return this == STATE || this == VASSAL;
    }

    public boolean canJoinOffensiveSiege() {
        return this != PEACEFUL;
    }
}
