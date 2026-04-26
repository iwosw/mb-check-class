package com.talhanation.bannermod.war.cooldown;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarCooldownRuntimeTest {

    private static final UUID ENTITY_A = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final UUID ENTITY_B = UUID.fromString("00000000-0000-0000-0000-0000000000a2");

    @Test
    void grantStoresExpiryAndFiresDirty() {
        WarCooldownRuntime runtime = new WarCooldownRuntime();
        AtomicInteger dirty = new AtomicInteger();
        runtime.setDirtyListener(dirty::incrementAndGet);

        runtime.grant(ENTITY_A, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 1000L);

        assertTrue(runtime.isActive(ENTITY_A, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 500L));
        assertEquals(1000L, runtime.endsAtFor(ENTITY_A, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 500L));
        assertEquals(1, dirty.get());
    }

    @Test
    void grantSamePairKeepsLongerExpiry() {
        WarCooldownRuntime runtime = new WarCooldownRuntime();
        AtomicInteger dirty = new AtomicInteger();
        runtime.setDirtyListener(dirty::incrementAndGet);

        runtime.grant(ENTITY_A, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 1000L);
        runtime.grant(ENTITY_A, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 800L);
        assertEquals(1, dirty.get(), "shorter expiry must not overwrite or fire dirty");
        assertEquals(1000L, runtime.endsAtFor(ENTITY_A, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 500L));

        runtime.grant(ENTITY_A, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 2000L);
        assertEquals(2000L, runtime.endsAtFor(ENTITY_A, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 500L));
        assertEquals(2, dirty.get());
    }

    @Test
    void differentEntitiesAndKindsAreIndependent() {
        WarCooldownRuntime runtime = new WarCooldownRuntime();
        runtime.grant(ENTITY_A, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 1000L);
        runtime.grant(ENTITY_A, WarCooldownKind.PEACEFUL_TOGGLE_RECENT, 2000L);
        runtime.grant(ENTITY_B, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 1500L);

        assertTrue(runtime.isActive(ENTITY_A, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 999L));
        assertTrue(runtime.isActive(ENTITY_A, WarCooldownKind.PEACEFUL_TOGGLE_RECENT, 1999L));
        assertTrue(runtime.isActive(ENTITY_B, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 1499L));
        assertFalse(runtime.isActive(ENTITY_B, WarCooldownKind.PEACEFUL_TOGGLE_RECENT, 0L));
    }

    @Test
    void expiredCooldownsDoNotReportActive() {
        WarCooldownRuntime runtime = new WarCooldownRuntime();
        runtime.grant(ENTITY_A, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 1000L);
        assertFalse(runtime.isActive(ENTITY_A, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 1000L));
        assertFalse(runtime.isActive(ENTITY_A, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 5000L));
    }

    @Test
    void pruneExpiredRemovesAndFiresDirtyOnlyWhenSomethingPruned() {
        WarCooldownRuntime runtime = new WarCooldownRuntime();
        AtomicInteger dirty = new AtomicInteger();
        runtime.setDirtyListener(dirty::incrementAndGet);

        runtime.grant(ENTITY_A, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 100L);
        runtime.grant(ENTITY_B, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 1000L);
        int initialDirty = dirty.get();

        assertEquals(0, runtime.pruneExpired(50L));
        assertEquals(initialDirty, dirty.get());

        assertEquals(1, runtime.pruneExpired(500L));
        assertEquals(initialDirty + 1, dirty.get());
        assertFalse(runtime.isActive(ENTITY_A, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 500L));
        assertTrue(runtime.isActive(ENTITY_B, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 500L));
    }

    @Test
    void tagRoundTripPreservesEntries() {
        WarCooldownRuntime runtime = new WarCooldownRuntime();
        runtime.grant(ENTITY_A, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 1000L);
        runtime.grant(ENTITY_B, WarCooldownKind.PEACEFUL_TOGGLE_RECENT, 2000L);

        CompoundTag tag = runtime.toTag();
        WarCooldownRuntime restored = WarCooldownRuntime.fromTag(tag);

        assertEquals(2, restored.all().size());
        assertTrue(restored.isActive(ENTITY_A, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 500L));
        assertTrue(restored.isActive(ENTITY_B, WarCooldownKind.PEACEFUL_TOGGLE_RECENT, 1500L));
    }
}
