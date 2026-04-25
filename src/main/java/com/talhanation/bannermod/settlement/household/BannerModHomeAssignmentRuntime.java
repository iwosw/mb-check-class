package com.talhanation.bannermod.settlement.household;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collection;
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
 * {@link HomePreference#SHARED}.
 */
public final class BannerModHomeAssignmentRuntime {

    /** Iteration-stable map so tests and debugging dumps stay deterministic. */
    private final Map<UUID, HomeAssignment> assignmentsByResident = new LinkedHashMap<>();
    private Runnable dirtyListener = () -> {
    };

    public void setDirtyListener(Runnable dirtyListener) {
        this.dirtyListener = dirtyListener == null ? () -> {
        } : dirtyListener;
    }

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
        if (assignment.equals(this.assignmentsByResident.get(residentUuid))) {
            return;
        }
        this.assignmentsByResident.put(residentUuid, assignment);
        markDirty();
    }

    /** Removes any binding for {@code residentUuid}. No-op if none existed. */
    public void clearAssignment(UUID residentUuid) {
        if (residentUuid == null) {
            return;
        }
        if (this.assignmentsByResident.remove(residentUuid) != null) {
            markDirty();
        }
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

    public List<HomeAssignment> snapshot() {
        return Collections.unmodifiableList(new ArrayList<>(this.assignmentsByResident.values()));
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        ListTag assignments = new ListTag();
        for (HomeAssignment assignment : snapshot()) {
            assignments.add(assignment.toTag());
        }
        tag.put("Assignments", assignments);
        return tag;
    }

    public static BannerModHomeAssignmentRuntime fromTag(CompoundTag tag) {
        BannerModHomeAssignmentRuntime runtime = new BannerModHomeAssignmentRuntime();
        List<HomeAssignment> assignments = new ArrayList<>();
        for (Tag entry : tag.getList("Assignments", Tag.TAG_COMPOUND)) {
            assignments.add(HomeAssignment.fromTag((CompoundTag) entry));
        }
        runtime.restoreSnapshot(assignments);
        return runtime;
    }

    public void restoreSnapshot(Collection<HomeAssignment> assignments) {
        List<HomeAssignment> before = snapshot();
        this.assignmentsByResident.clear();
        if (assignments != null) {
            for (HomeAssignment assignment : assignments) {
                if (assignment != null) {
                    this.assignmentsByResident.put(assignment.residentUuid(), assignment);
                }
            }
        }
        if (!before.equals(snapshot())) {
            markDirty();
        }
    }

    /** Drops every binding. Intended for test teardown and server shutdown. */
    public void reset() {
        if (!this.assignmentsByResident.isEmpty()) {
            this.assignmentsByResident.clear();
            markDirty();
        }
    }

    private void markDirty() {
        this.dirtyListener.run();
    }
}
