package com.talhanation.bannermod.citizen;

/**
 * Coarse citizen category — NOT profession. Profession granularity lives in
 * {@link CitizenProfession}. This enum is the top-level identity bucket used
 * by authority, settlement, and UI layers that only care whether the NPC is
 * civilian-ish or military-ish.
 *
 * <p>Legacy values {@link #RECRUIT} and {@link #WORKER} are kept as deprecated
 * aliases so callers written before the Cit-01 expansion continue to compile.
 * New callers must use the finer-grained values.
 */
public enum CitizenRole {

    /** Resident citizen of a settlement, not player-controlled as a tool. */
    CIVILIAN_RESIDENT,

    /** Player-owned worker NPC (old `workers` subsystem identity). */
    CONTROLLED_WORKER,

    /** Player-owned recruit NPC (old `recruits` subsystem identity). */
    CONTROLLED_RECRUIT,

    /** Settlement resident mobilised into temporary militia service. */
    MILITIA,

    /** Noble / leader / governor-eligible. */
    NOBLE,

    /** @deprecated use {@link #CONTROLLED_RECRUIT}. Retained for Cit-01 compat. */
    @Deprecated
    RECRUIT,

    /** @deprecated use {@link #CONTROLLED_WORKER}. Retained for Cit-01 compat. */
    @Deprecated
    WORKER;

    /**
     * Map a legacy two-value role to its post-expansion counterpart. Useful
     * when deserialising saved NBT written before Cit-01.
     */
    public static CitizenRole fromLegacy(CitizenRole legacy) {
        if (legacy == null) {
            return CIVILIAN_RESIDENT;
        }
        return switch (legacy) {
            case RECRUIT -> CONTROLLED_RECRUIT;
            case WORKER -> CONTROLLED_WORKER;
            default -> legacy;
        };
    }
}
