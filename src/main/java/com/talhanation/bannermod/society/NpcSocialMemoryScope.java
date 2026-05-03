package com.talhanation.bannermod.society;

public enum NpcSocialMemoryScope {
    PERSONAL,
    FAMILY,
    HOUSEHOLD,
    SETTLEMENT;

    public static NpcSocialMemoryScope fromName(String name) {
        if (name == null || name.isBlank()) {
            return PERSONAL;
        }
        try {
            return NpcSocialMemoryScope.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return PERSONAL;
        }
    }
}
