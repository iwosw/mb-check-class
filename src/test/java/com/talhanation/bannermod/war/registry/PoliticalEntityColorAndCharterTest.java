package com.talhanation.bannermod.war.registry;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure JUnit coverage for the UI-001 color and charter editing slice. Locks the validation
 * predicate (PoliticalRegistryValidation.validateColor / validateCharter), runtime mutator
 * semantics (no-op on identical / dirty-bump on change / reject on invalid), the record
 * withers, and NBT round-trip survival of the new fields through the existing
 * PoliticalEntityRecord serialization layer.
 */
class PoliticalEntityColorAndCharterTest {

    private static PoliticalRegistryRuntime fresh() {
        PoliticalRegistryRuntime runtime = new PoliticalRegistryRuntime();
        return runtime;
    }

    private static UUID seed(PoliticalRegistryRuntime runtime, String name) {
        return runtime.create(name, UUID.randomUUID(), BlockPos.ZERO,
                /* color */ "", /* charter */ "", "", "", 0L).orElseThrow().id();
    }

    // ------------------------------------------------------------------------
    // validateColor
    // ------------------------------------------------------------------------

    @Test
    void validateColorAcceptsEmptyAndNull() {
        assertTrue(PoliticalRegistryValidation.validateColor(null).valid());
        assertTrue(PoliticalRegistryValidation.validateColor("").valid());
        assertTrue(PoliticalRegistryValidation.validateColor("   ").valid());
    }

    @Test
    void validateColorAcceptsHexWithAndWithoutHash() {
        assertTrue(PoliticalRegistryValidation.validateColor("FF8800").valid());
        assertTrue(PoliticalRegistryValidation.validateColor("#ff8800").valid());
        assertTrue(PoliticalRegistryValidation.validateColor("80FF8800").valid());
        assertTrue(PoliticalRegistryValidation.validateColor("#80ff8800").valid());
    }

    @Test
    void validateColorRejectsBadLength() {
        assertFalse(PoliticalRegistryValidation.validateColor("F").valid());
        assertFalse(PoliticalRegistryValidation.validateColor("FF").valid());
        assertFalse(PoliticalRegistryValidation.validateColor("FFFFF").valid());
        assertFalse(PoliticalRegistryValidation.validateColor("FFFFFFF").valid());
        assertFalse(PoliticalRegistryValidation.validateColor("FFFFFFFFF").valid());
    }

    @Test
    void validateColorRejectsNonHexDigits() {
        assertFalse(PoliticalRegistryValidation.validateColor("ZZZZZZ").valid());
        assertFalse(PoliticalRegistryValidation.validateColor("FF88GG").valid());
        assertFalse(PoliticalRegistryValidation.validateColor("#GGGGGGGG").valid());
    }

    // ------------------------------------------------------------------------
    // validateCharter
    // ------------------------------------------------------------------------

    @Test
    void validateCharterAcceptsAnyTextUpToLimit() {
        assertTrue(PoliticalRegistryValidation.validateCharter(null).valid());
        assertTrue(PoliticalRegistryValidation.validateCharter("").valid());
        assertTrue(PoliticalRegistryValidation.validateCharter("free RP description").valid());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < PoliticalRegistryValidation.MAX_CHARTER_LENGTH; i++) sb.append('a');
        assertTrue(PoliticalRegistryValidation.validateCharter(sb.toString()).valid());
    }

    @Test
    void validateCharterRejectsOverLimit() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < PoliticalRegistryValidation.MAX_CHARTER_LENGTH + 1; i++) sb.append('a');
        var result = PoliticalRegistryValidation.validateCharter(sb.toString());
        assertFalse(result.valid());
        assertEquals("charter_too_long", result.reason());
    }

    // ------------------------------------------------------------------------
    // PoliticalEntityRecord withers
    // ------------------------------------------------------------------------

    @Test
    void recordWithColorReplacesOnlyColor() {
        PoliticalEntityRecord original = new PoliticalEntityRecord(
                UUID.randomUUID(), "Foo", PoliticalEntityStatus.SETTLEMENT,
                UUID.randomUUID(), java.util.List.of(), BlockPos.ZERO,
                "FF0000", "old charter", "", "", 0L, GovernmentForm.MONARCHY);
        PoliticalEntityRecord updated = original.withColor("00FF00");
        assertEquals("00FF00", updated.color());
        assertEquals("old charter", updated.charter());
        assertEquals(original.id(), updated.id());
        assertEquals(original.name(), updated.name());
    }

    @Test
    void recordWithCharterReplacesOnlyCharter() {
        PoliticalEntityRecord original = new PoliticalEntityRecord(
                UUID.randomUUID(), "Foo", PoliticalEntityStatus.SETTLEMENT,
                UUID.randomUUID(), java.util.List.of(), BlockPos.ZERO,
                "FF0000", "old charter", "", "", 0L, GovernmentForm.MONARCHY);
        PoliticalEntityRecord updated = original.withCharter("new charter text");
        assertEquals("new charter text", updated.charter());
        assertEquals("FF0000", updated.color());
    }

    // ------------------------------------------------------------------------
    // PoliticalRegistryRuntime mutators
    // ------------------------------------------------------------------------

    @Test
    void updateColorMutatesAndFiresDirty() {
        PoliticalRegistryRuntime runtime = fresh();
        boolean[] dirty = { false };
        runtime.setDirtyListener(() -> dirty[0] = true);
        UUID id = seed(runtime, "Foo");
        dirty[0] = false;

        assertTrue(runtime.updateColor(id, "FF00AA"));
        assertTrue(dirty[0]);
        assertEquals("FF00AA", runtime.byId(id).orElseThrow().color());
    }

    @Test
    void updateColorIdenticalIsNoopAndDoesNotFireDirty() {
        PoliticalRegistryRuntime runtime = fresh();
        UUID id = seed(runtime, "Foo");
        runtime.updateColor(id, "FF00AA");
        boolean[] dirty = { false };
        runtime.setDirtyListener(() -> dirty[0] = true);

        assertTrue(runtime.updateColor(id, "FF00AA"));
        assertFalse(dirty[0]);
    }

    @Test
    void updateColorRejectsInvalidHex() {
        PoliticalRegistryRuntime runtime = fresh();
        UUID id = seed(runtime, "Foo");
        assertFalse(runtime.updateColor(id, "ZZZZZZ"));
        assertEquals("", runtime.byId(id).orElseThrow().color(),
                "rejected color must not have mutated the record");
    }

    @Test
    void updateColorEmptyStringClears() {
        PoliticalRegistryRuntime runtime = fresh();
        UUID id = seed(runtime, "Foo");
        runtime.updateColor(id, "FF00AA");
        assertTrue(runtime.updateColor(id, ""));
        assertEquals("", runtime.byId(id).orElseThrow().color());
    }

    @Test
    void updateColorReturnsFalseForUnknownEntity() {
        PoliticalRegistryRuntime runtime = fresh();
        assertFalse(runtime.updateColor(UUID.randomUUID(), "FF00AA"));
    }

    @Test
    void updateCharterMutatesAndFiresDirty() {
        PoliticalRegistryRuntime runtime = fresh();
        boolean[] dirty = { false };
        runtime.setDirtyListener(() -> dirty[0] = true);
        UUID id = seed(runtime, "Foo");
        dirty[0] = false;

        assertTrue(runtime.updateCharter(id, "We hold these truths."));
        assertTrue(dirty[0]);
        assertEquals("We hold these truths.", runtime.byId(id).orElseThrow().charter());
    }

    @Test
    void updateCharterRejectsOverLimit() {
        PoliticalRegistryRuntime runtime = fresh();
        UUID id = seed(runtime, "Foo");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < PoliticalRegistryValidation.MAX_CHARTER_LENGTH + 5; i++) sb.append('x');
        assertFalse(runtime.updateCharter(id, sb.toString()));
        assertEquals("", runtime.byId(id).orElseThrow().charter());
    }

    @Test
    void updateCharterEmptyClears() {
        PoliticalRegistryRuntime runtime = fresh();
        UUID id = seed(runtime, "Foo");
        runtime.updateCharter(id, "First version");
        assertTrue(runtime.updateCharter(id, ""));
        assertEquals("", runtime.byId(id).orElseThrow().charter());
    }

    // ------------------------------------------------------------------------
    // NBT round-trip
    // ------------------------------------------------------------------------

    @Test
    void recordRoundTripPreservesColorAndCharter() {
        PoliticalRegistryRuntime runtime = fresh();
        UUID id = seed(runtime, "Foo");
        runtime.updateColor(id, "#80112233");
        runtime.updateCharter(id, "By and for the people");

        PoliticalRegistryRuntime restored = PoliticalRegistryRuntime.fromTag(runtime.toTag());
        PoliticalEntityRecord restoredRecord = restored.byId(id).orElseThrow();
        assertEquals("#80112233", restoredRecord.color());
        assertEquals("By and for the people", restoredRecord.charter());
    }
}
