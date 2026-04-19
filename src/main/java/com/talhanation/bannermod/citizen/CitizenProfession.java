package com.talhanation.bannermod.citizen;

import javax.annotation.Nullable;

/**
 * Fine-grained profession a {@link CitizenCore} is currently performing.
 * Swappable at runtime — designed so a single registered citizen entity can
 * change its behavior controller without a respawn.
 *
 * <p>Each value carries:
 * <ul>
 *   <li>the coarse {@link CitizenRole} bucket it belongs to</li>
 *   <li>the legacy per-profession entity id string (null for {@link #NONE}) —
 *       used by the save-migration slice (Cit-05) to map legacy
 *       {@code bannermod:bowman} / {@code bannermod:farmer} / etc. entities
 *       onto a post-migration {@code CitizenEntity} with matching
 *       profession</li>
 * </ul>
 */
public enum CitizenProfession {

    NONE(CitizenRole.CIVILIAN_RESIDENT, null),

    FARMER(CitizenRole.CONTROLLED_WORKER, "workers:farmer"),
    LUMBERJACK(CitizenRole.CONTROLLED_WORKER, "workers:lumberjack"),
    MINER(CitizenRole.CONTROLLED_WORKER, "workers:miner"),
    ANIMAL_FARMER(CitizenRole.CONTROLLED_WORKER, "workers:animal_farmer"),
    BUILDER(CitizenRole.CONTROLLED_WORKER, "workers:builder"),
    MERCHANT(CitizenRole.CONTROLLED_WORKER, "workers:merchant"),
    FISHERMAN(CitizenRole.CONTROLLED_WORKER, "workers:fisherman"),

    RECRUIT_SPEAR(CitizenRole.CONTROLLED_RECRUIT, "recruits:recruit"),
    RECRUIT_BOWMAN(CitizenRole.CONTROLLED_RECRUIT, "recruits:bowman"),
    RECRUIT_CROSSBOWMAN(CitizenRole.CONTROLLED_RECRUIT, "recruits:crossbowman"),
    RECRUIT_HORSEMAN(CitizenRole.CONTROLLED_RECRUIT, "recruits:horseman"),
    RECRUIT_NOMAD(CitizenRole.CONTROLLED_RECRUIT, "recruits:nomad"),
    RECRUIT_SCOUT(CitizenRole.CONTROLLED_RECRUIT, "recruits:scout"),
    RECRUIT_SHIELDMAN(CitizenRole.CONTROLLED_RECRUIT, "recruits:recruit_shieldman"),

    NOBLE(CitizenRole.NOBLE, "recruits:villager_noble");

    private final CitizenRole coarseRole;
    @Nullable
    private final String legacyEntityId;

    CitizenProfession(CitizenRole coarseRole, @Nullable String legacyEntityId) {
        this.coarseRole = coarseRole;
        this.legacyEntityId = legacyEntityId;
    }

    public CitizenRole coarseRole() {
        return this.coarseRole;
    }

    @Nullable
    public String legacyEntityId() {
        return this.legacyEntityId;
    }

    /**
     * Reverse lookup: legacy entity id string → profession. Returns
     * {@link #NONE} for unknown ids so the save-migration path has a
     * deterministic fallback.
     */
    public static CitizenProfession fromLegacyEntityId(@Nullable String legacyId) {
        if (legacyId == null) {
            return NONE;
        }
        for (CitizenProfession profession : values()) {
            if (legacyId.equals(profession.legacyEntityId)) {
                return profession;
            }
        }
        return NONE;
    }

    /**
     * Safe name parse — falls back to {@link #NONE} if {@code tagName} is
     * null, empty, or unknown. Matches the
     * {@link CitizenRole#fromLegacy(CitizenRole)} style so persistence paths
     * don't explode on a dirty save.
     */
    public static CitizenProfession fromTagName(@Nullable String tagName) {
        if (tagName == null || tagName.isEmpty()) {
            return NONE;
        }
        try {
            return CitizenProfession.valueOf(tagName);
        }
        catch (IllegalArgumentException ignored) {
            return NONE;
        }
    }
}
