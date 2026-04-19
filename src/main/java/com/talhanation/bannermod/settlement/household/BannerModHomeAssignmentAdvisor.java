package com.talhanation.bannermod.settlement.household;

import com.talhanation.bannermod.settlement.BannerModSettlementBuildingCategory;
import com.talhanation.bannermod.settlement.BannerModSettlementBuildingRecord;
import com.talhanation.bannermod.settlement.BannerModSettlementSnapshot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Deterministic helper that suggests a home building for a resident based on
 * the current settlement snapshot and existing assignments. Read-only: this
 * class never mutates the snapshot, record list, or runtime it inspects.
 *
 * <p>The advisor treats "housing-ish" buildings as candidates. The settlement
 * building category enum does not yet expose a dedicated HOUSING slot, so we
 * fall back to {@link BannerModSettlementBuildingCategory#GENERAL} and prefer
 * those entries. TODO: once HOUSING is introduced, swap the preference list.
 */
public final class BannerModHomeAssignmentAdvisor {

    /**
     * Preferred category for housing selection today. If the enum grows a
     * HOUSING value in a later slice, add it above GENERAL.
     * TODO category — replace GENERAL with HOUSING when the enum gains that value.
     */
    private static final BannerModSettlementBuildingCategory HOUSING_CATEGORY =
            BannerModSettlementBuildingCategory.GENERAL;

    private BannerModHomeAssignmentAdvisor() {
        // static helper
    }

    /**
     * Picks a building UUID that can host {@code residentUuid}, or empty if no
     * suitable slot exists in the snapshot.
     *
     * <p>Rules:
     * <ul>
     *   <li>Skip the resident's current home (already counted elsewhere).</li>
     *   <li>Skip buildings with {@code residentCapacity <= 0}.</li>
     *   <li>Prefer buildings in {@link #HOUSING_CATEGORY}, then fall through to
     *       any other category as a last resort so residents never sleep on
     *       the street during early-game testing.</li>
     *   <li>Skip buildings whose occupancy (existing assignments in the
     *       provided runtime) already meets {@code residentCapacity}.</li>
     *   <li>Iterate the snapshot's building list in its declared order so the
     *       choice is stable across ticks given identical input.</li>
     * </ul>
     */
    public static Optional<UUID> pickHomeBuilding(UUID residentUuid,
                                                  BannerModSettlementSnapshot snapshot,
                                                  BannerModHomeAssignmentRuntime existing) {
        if (residentUuid == null || snapshot == null || existing == null) {
            return Optional.empty();
        }
        List<BannerModSettlementBuildingRecord> buildings = snapshot.buildings();
        if (buildings == null || buildings.isEmpty()) {
            return Optional.empty();
        }
        Optional<UUID> preferred = scan(residentUuid, buildings, existing, HOUSING_CATEGORY, true);
        if (preferred.isPresent()) {
            return preferred;
        }
        // Fallback: any building with capacity to spare, regardless of category.
        return scan(residentUuid, buildings, existing, null, false);
    }

    private static Optional<UUID> scan(UUID residentUuid,
                                       List<BannerModSettlementBuildingRecord> buildings,
                                       BannerModHomeAssignmentRuntime existing,
                                       BannerModSettlementBuildingCategory required,
                                       boolean enforceCategory) {
        UUID currentHome = existing.homeFor(residentUuid)
                .map(HomeAssignment::homeBuildingUuid)
                .orElse(null);
        for (BannerModSettlementBuildingRecord building : buildings) {
            if (building == null || building.buildingUuid() == null) {
                continue;
            }
            if (building.residentCapacity() <= 0) {
                continue;
            }
            if (enforceCategory && required != null && building.buildingCategory() != required) {
                continue;
            }
            if (building.buildingUuid().equals(currentHome)) {
                continue;
            }
            int occupancy = existing.assignmentsForBuilding(building.buildingUuid()).size();
            if (occupancy >= building.residentCapacity()) {
                continue;
            }
            return Optional.of(building.buildingUuid());
        }
        return Optional.empty();
    }
}
