package com.talhanation.bannermod.combat;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormationPlannerTest {

    @Test
    void slotForCoversEveryCombatRole() {
        assertEquals(FormationSlot.FRONT_RANK, FormationPlanner.slotFor(CombatRole.INFANTRY));
        assertEquals(FormationSlot.SUPPORT_RANK, FormationPlanner.slotFor(CombatRole.PIKE));
        assertEquals(FormationSlot.REAR_RANK, FormationPlanner.slotFor(CombatRole.RANGED));
        assertEquals(FormationSlot.FLANK, FormationPlanner.slotFor(CombatRole.CAVALRY));
        assertEquals(FormationSlot.UNASSIGNED, FormationPlanner.slotFor(null));
    }

    @Test
    void mixedSquadFormsLayeredLines() {
        // Five infantry, two pike, three ranged, two cavalry.
        List<CombatRole> roles = List.of(
                CombatRole.INFANTRY, CombatRole.INFANTRY, CombatRole.INFANTRY,
                CombatRole.INFANTRY, CombatRole.INFANTRY,
                CombatRole.PIKE, CombatRole.PIKE,
                CombatRole.RANGED, CombatRole.RANGED, CombatRole.RANGED,
                CombatRole.CAVALRY, CombatRole.CAVALRY
        );

        Map<FormationSlot, Integer> assignment = FormationPlanner.assign(roles);

        assertEquals(5, assignment.get(FormationSlot.FRONT_RANK));
        assertEquals(2, assignment.get(FormationSlot.SUPPORT_RANK));
        assertEquals(3, assignment.get(FormationSlot.REAR_RANK));
        assertEquals(2, assignment.get(FormationSlot.FLANK));
        assertEquals(0, assignment.get(FormationSlot.UNASSIGNED));
    }

    @Test
    void shieldWallReadyOnlyFiresAtThreshold() {
        Map<FormationSlot, Integer> below = FormationPlanner.assign(List.of(
                CombatRole.INFANTRY, CombatRole.INFANTRY));
        assertFalse(FormationPlanner.shieldWallReady(below));

        Map<FormationSlot, Integer> exact = FormationPlanner.assign(List.of(
                CombatRole.INFANTRY, CombatRole.INFANTRY, CombatRole.INFANTRY));
        assertTrue(FormationPlanner.shieldWallReady(exact));

        Map<FormationSlot, Integer> overflow = FormationPlanner.assign(List.of(
                CombatRole.INFANTRY, CombatRole.INFANTRY, CombatRole.INFANTRY,
                CombatRole.INFANTRY, CombatRole.INFANTRY));
        assertTrue(FormationPlanner.shieldWallReady(overflow));
    }

    @Test
    void shieldWallNotReadyOnEmptyOrNullAssignment() {
        assertFalse(FormationPlanner.shieldWallReady(FormationPlanner.assign(List.of())));
        assertFalse(FormationPlanner.shieldWallReady(null));
    }

    @Test
    void unitInsideCohesionRadiusKeepsFormationBenefits() {
        assertFalse(FormationPlanner.isIsolated(0.0D));
        assertFalse(FormationPlanner.isIsolated(FormationPlanner.FORMATION_COHESION_RADIUS));
        assertEquals(1.0D, FormationPlanner.cohesionMultiplier(2.0D), 1e-9);
    }

    @Test
    void unitOutsideCohesionRadiusLosesFormationBenefits() {
        assertTrue(FormationPlanner.isIsolated(FormationPlanner.FORMATION_COHESION_RADIUS + 0.1D));
        assertEquals(FormationPlanner.ISOLATION_PENALTY_MULTIPLIER,
                FormationPlanner.cohesionMultiplier(50.0D), 1e-9);
    }

    @Test
    void negativeOrNanDistanceCountsAsIsolated() {
        // Defensive: a buggy caller passing -1 / NaN must count as isolated, not as
        // "perfectly aligned with the slot anchor".
        assertTrue(FormationPlanner.isIsolated(-1.0D));
        assertTrue(FormationPlanner.isIsolated(Double.NaN));
        assertEquals(FormationPlanner.ISOLATION_PENALTY_MULTIPLIER,
                FormationPlanner.cohesionMultiplier(-1.0D), 1e-9);
    }

    @Test
    void infiniteDistanceCountsAsIsolated() {
        // POSITIVE_INFINITY is the documented "no anchor found" signal.
        assertTrue(FormationPlanner.isIsolated(Double.POSITIVE_INFINITY));
    }

    @Test
    void assignTreatsNullRoleListAsEmptyButReturnsAllSlots() {
        Map<FormationSlot, Integer> assignment = FormationPlanner.assign(null);

        for (FormationSlot slot : FormationSlot.values()) {
            assertEquals(0, assignment.get(slot), "slot " + slot + " missing or non-zero");
        }
    }

    @Test
    void enumMapPreservesAssignmentValues() {
        Map<FormationSlot, Integer> assignment = FormationPlanner.assign(List.of(
                CombatRole.INFANTRY, CombatRole.PIKE, CombatRole.RANGED, CombatRole.CAVALRY));

        Map<FormationSlot, Integer> enumMap = FormationPlanner.toEnumMap(assignment);

        assertEquals(1, enumMap.get(FormationSlot.FRONT_RANK));
        assertEquals(1, enumMap.get(FormationSlot.SUPPORT_RANK));
        assertEquals(1, enumMap.get(FormationSlot.REAR_RANK));
        assertEquals(1, enumMap.get(FormationSlot.FLANK));
        assertEquals(0, enumMap.get(FormationSlot.UNASSIGNED));
    }

    @Test
    void enumMapHandlesNullInput() {
        Map<FormationSlot, Integer> enumMap = FormationPlanner.toEnumMap(null);
        for (FormationSlot slot : FormationSlot.values()) {
            assertEquals(0, enumMap.get(slot));
        }
    }
}
