package com.talhanation.bannermod.war.registry;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PoliticalRegistryRuntimeRenameTest {

    private static final UUID LEADER_A = UUID.fromString("00000000-0000-0000-0000-0000000000aa");
    private static final UUID LEADER_B = UUID.fromString("00000000-0000-0000-0000-0000000000bb");
    private static final BlockPos CAPITAL = new BlockPos(0, 64, 0);

    @Test
    void updateNameAcceptsValidUniqueName() {
        PoliticalRegistryRuntime runtime = new PoliticalRegistryRuntime();
        AtomicInteger dirtyCount = new AtomicInteger();
        runtime.setDirtyListener(dirtyCount::incrementAndGet);
        PoliticalEntityRecord created = runtime.create("Acadia", LEADER_A, CAPITAL, "", "", "", "", 0L).orElseThrow();
        int initialDirty = dirtyCount.get();

        boolean ok = runtime.updateName(created.id(), "Acadia Republic");

        assertTrue(ok);
        assertEquals("Acadia Republic", runtime.byId(created.id()).orElseThrow().name());
        assertEquals(initialDirty + 1, dirtyCount.get());
    }

    @Test
    void updateNameRejectsDuplicateOfDifferentEntity() {
        PoliticalRegistryRuntime runtime = new PoliticalRegistryRuntime();
        runtime.create("Acadia", LEADER_A, CAPITAL, "", "", "", "", 0L);
        PoliticalEntityRecord second = runtime.create("Brittany", LEADER_B, CAPITAL, "", "", "", "", 0L).orElseThrow();

        PoliticalRegistryValidation.Result validation = runtime.canRename(second.id(), "acadia");
        assertFalse(validation.valid());
        assertEquals("duplicate_name", validation.reason());

        assertFalse(runtime.updateName(second.id(), "acadia"));
        assertEquals("Brittany", runtime.byId(second.id()).orElseThrow().name());
    }

    @Test
    void updateNameRejectsTooShortAndTooLongNames() {
        PoliticalRegistryRuntime runtime = new PoliticalRegistryRuntime();
        PoliticalEntityRecord created = runtime.create("Acadia", LEADER_A, CAPITAL, "", "", "", "", 0L).orElseThrow();

        assertFalse(runtime.updateName(created.id(), "a"));
        assertFalse(runtime.updateName(created.id(),
                "0123456789012345678901234567890123456789"));
        assertEquals("Acadia", runtime.byId(created.id()).orElseThrow().name());
    }

    @Test
    void updateNameAllowsRenamingToOwnNormalizedName() {
        PoliticalRegistryRuntime runtime = new PoliticalRegistryRuntime();
        PoliticalEntityRecord created = runtime.create("Acadia", LEADER_A, CAPITAL, "", "", "", "", 0L).orElseThrow();
        AtomicInteger dirtyCount = new AtomicInteger();
        runtime.setDirtyListener(dirtyCount::incrementAndGet);

        assertTrue(runtime.updateName(created.id(), "  Acadia  "));
        assertEquals("Acadia", runtime.byId(created.id()).orElseThrow().name());
        assertEquals(0, dirtyCount.get());
    }

    @Test
    void updateNameRejectsUnknownEntityWithoutFiringDirty() {
        PoliticalRegistryRuntime runtime = new PoliticalRegistryRuntime();
        AtomicInteger dirtyCount = new AtomicInteger();
        runtime.setDirtyListener(dirtyCount::incrementAndGet);

        Optional<PoliticalEntityRecord> missing = runtime.byId(UUID.randomUUID());
        assertNotNull(missing);
        assertFalse(missing.isPresent());

        assertFalse(runtime.updateName(UUID.randomUUID(), "AnyName"));
        assertEquals(0, dirtyCount.get());
    }
}
