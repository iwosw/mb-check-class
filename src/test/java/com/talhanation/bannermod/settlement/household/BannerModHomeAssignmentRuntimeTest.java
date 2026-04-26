package com.talhanation.bannermod.settlement.household;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerModHomeAssignmentRuntimeTest {

    private static final UUID RESIDENT_A = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final UUID RESIDENT_B = UUID.fromString("00000000-0000-0000-0000-0000000000a2");
    private static final UUID RESIDENT_C = UUID.fromString("00000000-0000-0000-0000-0000000000a3");
    private static final UUID HOUSE_1 = UUID.fromString("00000000-0000-0000-0000-0000000000b1");
    private static final UUID HOUSE_2 = UUID.fromString("00000000-0000-0000-0000-0000000000b2");

    @Test
    void assignStoresAndHomeForRetrieves() {
        BannerModHomeAssignmentRuntime runtime = new BannerModHomeAssignmentRuntime();

        runtime.assign(RESIDENT_A, HOUSE_1, HomePreference.ASSIGNED, 500L);

        Optional<HomeAssignment> home = runtime.homeFor(RESIDENT_A);
        assertTrue(home.isPresent());
        assertEquals(RESIDENT_A, home.get().residentUuid());
        assertEquals(HOUSE_1, home.get().homeBuildingUuid());
        assertEquals(HomePreference.ASSIGNED, home.get().preference());
        assertEquals(500L, home.get().assignedAtGameTime());
        assertEquals(1, runtime.totalAssignments());
    }

    @Test
    void reassigningReplacesPreviousBinding() {
        BannerModHomeAssignmentRuntime runtime = new BannerModHomeAssignmentRuntime();
        runtime.assign(RESIDENT_A, HOUSE_1, HomePreference.ASSIGNED, 100L);

        runtime.assign(RESIDENT_A, HOUSE_2, HomePreference.TEMPORARY_SHELTER, 200L);

        assertEquals(1, runtime.totalAssignments());
        HomeAssignment current = runtime.homeFor(RESIDENT_A).orElseThrow();
        assertEquals(HOUSE_2, current.homeBuildingUuid());
        assertEquals(HomePreference.TEMPORARY_SHELTER, current.preference());
        assertEquals(200L, current.assignedAtGameTime());
    }

    @Test
    void clearAssignmentRemovesJustThatResident() {
        BannerModHomeAssignmentRuntime runtime = new BannerModHomeAssignmentRuntime();
        runtime.assign(RESIDENT_A, HOUSE_1, HomePreference.ASSIGNED, 0L);
        runtime.assign(RESIDENT_B, HOUSE_2, HomePreference.ASSIGNED, 0L);

        runtime.clearAssignment(RESIDENT_A);

        assertFalse(runtime.homeFor(RESIDENT_A).isPresent());
        assertTrue(runtime.homeFor(RESIDENT_B).isPresent());
        assertEquals(1, runtime.totalAssignments());
    }

    @Test
    void multipleResidentsSharingHomeShowUnderAssignmentsForBuilding() {
        BannerModHomeAssignmentRuntime runtime = new BannerModHomeAssignmentRuntime();
        runtime.assign(RESIDENT_A, HOUSE_1, HomePreference.SHARED, 10L);
        runtime.assign(RESIDENT_B, HOUSE_1, HomePreference.SHARED, 11L);
        runtime.assign(RESIDENT_C, HOUSE_2, HomePreference.ASSIGNED, 12L);

        List<HomeAssignment> house1Residents = runtime.assignmentsForBuilding(HOUSE_1);
        List<HomeAssignment> house2Residents = runtime.assignmentsForBuilding(HOUSE_2);

        assertEquals(2, house1Residents.size());
        assertTrue(house1Residents.stream().anyMatch(a -> a.residentUuid().equals(RESIDENT_A)));
        assertTrue(house1Residents.stream().anyMatch(a -> a.residentUuid().equals(RESIDENT_B)));
        for (HomeAssignment a : house1Residents) {
            assertEquals(HomePreference.SHARED, a.preference());
        }
        assertEquals(1, house2Residents.size());
        assertEquals(RESIDENT_C, house2Residents.get(0).residentUuid());
    }

    @Test
    void assignmentsForBuildingReturnsEmptyListForUnknownBuilding() {
        BannerModHomeAssignmentRuntime runtime = new BannerModHomeAssignmentRuntime();
        runtime.assign(RESIDENT_A, HOUSE_1, HomePreference.ASSIGNED, 0L);

        List<HomeAssignment> unknown = runtime.assignmentsForBuilding(UUID.randomUUID());

        assertNotNull(unknown);
        assertTrue(unknown.isEmpty());
    }

    @Test
    void resetClearsEverything() {
        BannerModHomeAssignmentRuntime runtime = new BannerModHomeAssignmentRuntime();
        runtime.assign(RESIDENT_A, HOUSE_1, HomePreference.ASSIGNED, 0L);
        runtime.assign(RESIDENT_B, HOUSE_2, HomePreference.SHARED, 0L);

        runtime.reset();

        assertEquals(0, runtime.totalAssignments());
        assertFalse(runtime.homeFor(RESIDENT_A).isPresent());
        assertFalse(runtime.homeFor(RESIDENT_B).isPresent());
        assertTrue(runtime.assignmentsForBuilding(HOUSE_1).isEmpty());
    }

    @Test
    void homeForNullResidentReturnsEmpty() {
        BannerModHomeAssignmentRuntime runtime = new BannerModHomeAssignmentRuntime();

        Optional<HomeAssignment> home = runtime.homeFor(null);

        assertSame(Optional.empty(), home);
    }

    @Test
    void nbtRoundTripRestoresAssignmentsInOrder() {
        BannerModHomeAssignmentRuntime runtime = new BannerModHomeAssignmentRuntime();
        runtime.assign(RESIDENT_A, HOUSE_1, HomePreference.ASSIGNED, 10L);
        runtime.assign(RESIDENT_B, HOUSE_2, HomePreference.SHARED, 20L);

        BannerModHomeAssignmentRuntime restored = BannerModHomeAssignmentRuntime.fromTag(runtime.toTag());

        assertEquals(2, restored.totalAssignments());
        List<HomeAssignment> snapshot = restored.snapshot();
        assertEquals(RESIDENT_A, snapshot.get(0).residentUuid());
        assertEquals(HOUSE_1, restored.homeFor(RESIDENT_A).orElseThrow().homeBuildingUuid());
        assertEquals(HomePreference.SHARED, restored.homeFor(RESIDENT_B).orElseThrow().preference());
        assertEquals(20L, restored.homeFor(RESIDENT_B).orElseThrow().assignedAtGameTime());
    }

    @Test
    void mutationsMarkDirty() {
        BannerModHomeAssignmentRuntime runtime = new BannerModHomeAssignmentRuntime();
        AtomicInteger dirtyCount = new AtomicInteger();
        runtime.setDirtyListener(dirtyCount::incrementAndGet);

        runtime.assign(RESIDENT_A, HOUSE_1, HomePreference.ASSIGNED, 10L);
        runtime.clearAssignment(UUID.randomUUID());
        runtime.clearAssignment(RESIDENT_A);

        assertEquals(2, dirtyCount.get());
    }

    @Test
    void identicalAssignAndRestoreDoNotDirtyAgain() {
        BannerModHomeAssignmentRuntime runtime = new BannerModHomeAssignmentRuntime();
        AtomicInteger dirtyCount = new AtomicInteger();
        runtime.setDirtyListener(dirtyCount::incrementAndGet);

        runtime.assign(RESIDENT_A, HOUSE_1, HomePreference.ASSIGNED, 10L);
        runtime.assign(RESIDENT_A, HOUSE_1, HomePreference.ASSIGNED, 10L);

        assertEquals(1, dirtyCount.get());

        List<HomeAssignment> snapshot = runtime.snapshot();
        runtime.restoreSnapshot(snapshot);

        assertEquals(1, dirtyCount.get());

        runtime.restoreSnapshot(List.of());

        assertEquals(2, dirtyCount.get());
    }
}
