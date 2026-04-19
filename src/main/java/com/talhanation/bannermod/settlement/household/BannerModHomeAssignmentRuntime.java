package com.talhanation.bannermod.settlement.household;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Server-side in-memory coordinator for resident-to-home bindings.
 *
 * <p>Bounded to at most one assignment per resident. Multiple residents may
 * share a single home; callers signal that intent through
 * {@link HomePreference#SHARED}. This slice is additive and has no persistence:
 * bindings evaporate on server restart.
 *
 * <p>TODO persistence — reissue assignments from the saved snapshot once a
 * home-ledger component exists.
 */
public final class BannerModHomeAssignmentRuntime {

    /** Iteration-stable map so tests and debugging dumps stay deterministic. */
    private final Map<UUID, HomeAssignment> assignmentsByResident = new LinkedHashMap<>();

    /**
     * @return the current binding for {@code residentUuid}, or empty if the
     *     resident has no home on record.
     */
    public Optional<HomeAssignment> homeFor(UUID residentUuid) {
        if (residentUuid == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.assignmentsByResident.get(residentUuid));
    }

    /**
     * Grants {@code residentUuid} a binding to {@code homeBuildingUuid}. If the
     * resident already had a home, the old binding is replaced.
     */
    public void assign(UUID residentUuid,
                       UUID homeBuildingUuid,
                       HomePreference preference,
                       long gameTime) {
        if (residentUuid == null) {
            throw new IllegalArgumentException("residentUuid must not be null");
        }
        if (homeBuildingUuid == null) {
            throw new IllegalArgumentException("homeBuildingUuid must not be null");
        }
        HomePreference effectivePreference = preference == null ? HomePreference.NONE : preference;
        HomeAssignment assignment = new HomeAssignment(residentUuid, homeBuildingUuid, gameTime, effectivePreference);
        this.assignmentsByResident.put(residentUuid, assignment);
    }

    /** Removes any binding for {@code residentUuid}. No-op if none existed. */
    public void clearAssignment(UUID residentUuid) {
        if (residentUuid == null) {
            return;
        }
        this.assignmentsByResident.remove(residentUuid);
    }

    /** @return number of residents currently bound to a home. */
    public int totalAssignments() {
        return this.assignmentsByResident.size();
    }

    /**
     * @return every active binding pointing at {@code buildingUuid}, in
     *     insertion order; empty list if none.
     */
    public List<HomeAssignment> assignmentsForBuilding(UUID buildingUuid) {
        if (buildingUuid == null) {
            return Collections.emptyList();
        }
        List<HomeAssignment> matches = new ArrayList<>();
        for (HomeAssignment assignment : this.assignmentsByResident.values()) {
            if (buildingUuid.equals(assignment.homeBuildingUuid())) {
                matches.add(assignment);
            }
        }
        return matches;
    }

    /** Drops every binding. Intended for test teardown and server shutdown. */
    public void reset() {
        this.assignmentsByResident.clear();
    }
}
