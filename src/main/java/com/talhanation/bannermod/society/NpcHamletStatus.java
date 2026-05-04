package com.talhanation.bannermod.society;

public enum NpcHamletStatus {
    INFORMAL,
    REGISTERED,
    ABANDONED;

    public static NpcHamletStatus fromName(String name) {
        if (name == null || name.isBlank()) {
            return INFORMAL;
        }
        try {
            return NpcHamletStatus.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return INFORMAL;
        }
    }
}
