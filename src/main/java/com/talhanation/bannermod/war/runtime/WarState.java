package com.talhanation.bannermod.war.runtime;

public enum WarState {
    DECLARED,
    ACTIVE,
    IN_SIEGE_WINDOW,
    RESOLVED,
    CANCELLED;

    public boolean allowsBattleWindowActivation() {
        return this == DECLARED || this == ACTIVE || this == IN_SIEGE_WINDOW;
    }
}
