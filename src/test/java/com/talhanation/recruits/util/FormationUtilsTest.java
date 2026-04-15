package com.talhanation.recruits.util;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormationUtilsTest {

    @Test
    void chooseNearestFreeFormationSlotSkipsCurrentAndOccupiedSlots() {
        List<FormationUtils.FormationFallbackSlot> slots = List.of(
                new FormationUtils.FormationFallbackSlot(0, new Vec3(0.0D, 64.0D, 0.0D), false),
                new FormationUtils.FormationFallbackSlot(1, new Vec3(6.0D, 64.0D, 0.0D), true),
                new FormationUtils.FormationFallbackSlot(2, new Vec3(4.0D, 64.0D, 0.0D), false),
                new FormationUtils.FormationFallbackSlot(3, new Vec3(12.0D, 64.0D, 0.0D), false)
        );

        Optional<FormationUtils.FormationFallbackDecision> decision = FormationUtils.chooseNearestFreeFormationSlot(
                new Vec3(4.8D, 64.0D, 0.0D),
                0,
                slots
        );

        assertTrue(decision.isPresent());
        assertEquals(0, decision.get().fromSlotIndex());
        assertEquals(2, decision.get().toSlotIndex());
    }

    @Test
    void chooseNearestFreeFormationSlotReturnsEmptyWhenNoAlternativeExists() {
        List<FormationUtils.FormationFallbackSlot> slots = List.of(
                new FormationUtils.FormationFallbackSlot(0, new Vec3(0.0D, 64.0D, 0.0D), false),
                new FormationUtils.FormationFallbackSlot(1, new Vec3(2.0D, 64.0D, 0.0D), true)
        );

        Optional<FormationUtils.FormationFallbackDecision> decision = FormationUtils.chooseNearestFreeFormationSlot(
                new Vec3(1.0D, 64.0D, 0.0D),
                0,
                slots
        );

        assertTrue(decision.isEmpty());
    }
}
