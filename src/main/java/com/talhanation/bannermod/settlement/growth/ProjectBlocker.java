package com.talhanation.bannermod.settlement.growth;

/**
 * Soft reason a {@link PendingProject} cannot currently be started. Surfaced so
 * a downstream execution slice can decide whether to wait, retry, or drop the
 * candidate. {@link #NONE} signals the project is eligible to start.
 */
public enum ProjectBlocker {
    NONE,
    NO_BUILDER,
    NO_MATERIALS,
    NO_SITE,
    UNDER_SIEGE,
    POPULATION_CAP
}
