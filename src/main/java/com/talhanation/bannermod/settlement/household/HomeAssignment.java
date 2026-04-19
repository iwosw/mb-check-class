package com.talhanation.bannermod.settlement.household;

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
}
