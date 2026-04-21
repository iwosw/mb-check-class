package com.talhanation.bannermod.ai.military;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step 1.C: the registry is the authoritative source for slot-owner lookup in gap-fill.
 */
class FormationSlotRegistryTest {

    private FormationTargetSelectionController.CohortKey cohort;
    private UUID alpha;
    private UUID beta;

    @BeforeEach
    void setUp() {
        FormationSlotRegistry.clearAll();
        cohort = new FormationTargetSelectionController.CohortKey(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                UUID.fromString("00000000-0000-0000-0000-000000000002")
        );
        alpha = UUID.fromString("00000000-0000-0000-0000-00000000aaaa");
        beta = UUID.fromString("00000000-0000-0000-0000-00000000bbbb");
    }

    @AfterEach
    void tearDown() {
        FormationSlotRegistry.clearAll();
    }

    @Test
    void assignStoresOwnerAndPosition() {
        Vec3 pos = new Vec3(1, 64, 2);
        FormationSlotRegistry.assign(cohort, 5, alpha, pos, 90f);

        Map<Integer, FormationSlotRegistry.SlotEntry> slots = FormationSlotRegistry.slotsOf(cohort);
        assertEquals(1, slots.size());
        FormationSlotRegistry.SlotEntry entry = slots.get(5);
        assertEquals(alpha, entry.ownerId());
        assertEquals(pos, entry.holdPos());
        assertEquals(90f, entry.ownerRotDeg());
    }

    @Test
    void assignOverwritesExistingSlotOwner() {
        Vec3 pos = new Vec3(0, 0, 0);
        FormationSlotRegistry.assign(cohort, 1, alpha, pos, 0f);
        FormationSlotRegistry.assign(cohort, 1, beta, pos, 0f);

        assertEquals(beta, FormationSlotRegistry.slotsOf(cohort).get(1).ownerId());
    }

    @Test
    void removeDropsSingleSlotOnly() {
        Vec3 pos = new Vec3(0, 0, 0);
        FormationSlotRegistry.assign(cohort, 1, alpha, pos, 0f);
        FormationSlotRegistry.assign(cohort, 2, beta, pos, 0f);

        FormationSlotRegistry.remove(cohort, 1);
        Map<Integer, FormationSlotRegistry.SlotEntry> slots = FormationSlotRegistry.slotsOf(cohort);
        assertFalse(slots.containsKey(1));
        assertTrue(slots.containsKey(2));
    }

    @Test
    void clearWipesEntireCohort() {
        Vec3 pos = new Vec3(0, 0, 0);
        FormationSlotRegistry.assign(cohort, 1, alpha, pos, 0f);
        FormationSlotRegistry.clear(cohort);
        assertTrue(FormationSlotRegistry.slotsOf(cohort).isEmpty());
    }

    @Test
    void assignWithNullsIsNoOp() {
        Vec3 pos = new Vec3(0, 0, 0);
        FormationSlotRegistry.assign(null, 1, alpha, pos, 0f);
        FormationSlotRegistry.assign(cohort, 1, null, pos, 0f);
        FormationSlotRegistry.assign(cohort, 1, alpha, null, 0f);
        FormationSlotRegistry.assign(cohort, -1, alpha, pos, 0f);

        assertTrue(FormationSlotRegistry.slotsOf(cohort).isEmpty());
    }

    @Test
    void snapshotIsDetachedFromLiveState() {
        Vec3 pos = new Vec3(0, 0, 0);
        FormationSlotRegistry.assign(cohort, 1, alpha, pos, 0f);
        Map<Integer, FormationSlotRegistry.SlotEntry> snapshot = FormationSlotRegistry.slotsOf(cohort);

        // Mutating the live registry must not change a prior snapshot.
        FormationSlotRegistry.assign(cohort, 1, beta, pos, 0f);
        assertEquals(alpha, snapshot.get(1).ownerId());
    }

    @Test
    void unknownCohortReturnsEmptyMap() {
        assertTrue(FormationSlotRegistry.slotsOf(cohort).isEmpty());
        assertNull(FormationSlotRegistry.slotsOf(cohort).get(0));
    }
}
