package com.talhanation.bannermod.society;

public enum NpcSocialMemoryType {
    ASSAULTED_BY_PLAYER,
    PROTECTED_BY_PLAYER,
    HAMLET_ATTACKED_BY_PLAYER,
    STARVED,
    HOMELESS,
    OVERCROWDED;

    public static NpcSocialMemoryType fromName(String name) {
        if (name == null || name.isBlank()) {
            return ASSAULTED_BY_PLAYER;
        }
        try {
            return NpcSocialMemoryType.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return ASSAULTED_BY_PLAYER;
        }
    }
}
