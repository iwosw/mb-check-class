package com.talhanation.bannermod.settlement.household;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/**
 * In-memory record binding a resident to a home building.
 *
 * <p>Holds the game tick at which the binding was granted so that callers can
 * measure tenure without scanning history. Immutable — replaced wholesale when
 * a resident is reassigned.
 */
public record HomeAssignment(
        UUID residentUuid,
        UUID homeBuildingUuid,
        long assignedAtGameTime,
        HomePreference preference
) {
    public HomeAssignment {
        if (residentUuid == null) {
            throw new IllegalArgumentException("residentUuid must not be null");
        }
        if (homeBuildingUuid == null) {
            throw new IllegalArgumentException("homeBuildingUuid must not be null");
        }
        if (preference == null) {
            preference = HomePreference.NONE;
        }
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("ResidentUuid", this.residentUuid);
        tag.putUUID("HomeBuildingUuid", this.homeBuildingUuid);
        tag.putLong("AssignedAtGameTime", this.assignedAtGameTime);
        tag.putString("Preference", this.preference.name());
        return tag;
    }

    public static HomeAssignment fromTag(CompoundTag tag) {
        return new HomeAssignment(
                tag.getUUID("ResidentUuid"),
                tag.getUUID("HomeBuildingUuid"),
                tag.getLong("AssignedAtGameTime"),
                preferenceFromTagName(tag.getString("Preference"))
        );
    }

    private static HomePreference preferenceFromTagName(String name) {
        try {
            return HomePreference.valueOf(name);
        } catch (IllegalArgumentException | NullPointerException exception) {
            return HomePreference.NONE;
        }
    }
}
