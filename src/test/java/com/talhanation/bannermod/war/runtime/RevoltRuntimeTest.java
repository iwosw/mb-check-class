package com.talhanation.bannermod.war.runtime;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RevoltRuntimeTest {

    @Test
    void scheduleRejectsSelfRevolt() {
        RevoltRuntime runtime = new RevoltRuntime();
        UUID a = UUID.randomUUID();
        Optional<RevoltRecord> result = runtime.schedule(UUID.randomUUID(), a, a, 0L);
        assertTrue(result.isEmpty());
    }

    @Test
    void scheduleRejectsDuplicatePending() {
        RevoltRuntime runtime = new RevoltRuntime();
        UUID occupationId = UUID.randomUUID();
        UUID rebel = UUID.randomUUID();
        UUID occupier = UUID.randomUUID();
        assertTrue(runtime.schedule(occupationId, rebel, occupier, 0L).isPresent());
        assertTrue(runtime.schedule(occupationId, rebel, occupier, 1L).isEmpty());
    }

    @Test
    void resolveTransitionsState() {
        RevoltRuntime runtime = new RevoltRuntime();
        RevoltRecord record = runtime.schedule(UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), 0L).orElseThrow();
        assertTrue(runtime.resolve(record.id(), RevoltState.SUCCESS, 100L));
        assertEquals(RevoltState.SUCCESS, runtime.byId(record.id()).orElseThrow().state());
    }

    @Test
    void resolveRejectsAlreadyResolved() {
        RevoltRuntime runtime = new RevoltRuntime();
        RevoltRecord record = runtime.schedule(UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), 0L).orElseThrow();
        runtime.resolve(record.id(), RevoltState.SUCCESS, 100L);
        assertFalse(runtime.resolve(record.id(), RevoltState.FAILED, 200L));
    }

    @Test
    void resolveRejectsPendingTarget() {
        RevoltRuntime runtime = new RevoltRuntime();
        RevoltRecord record = runtime.schedule(UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), 0L).orElseThrow();
        assertFalse(runtime.resolve(record.id(), RevoltState.PENDING, 100L));
    }

    @Test
    void hasPendingRevoltForOccupation() {
        RevoltRuntime runtime = new RevoltRuntime();
        UUID occupationId = UUID.randomUUID();
        RevoltRecord record = runtime.schedule(occupationId,
                UUID.randomUUID(), UUID.randomUUID(), 0L).orElseThrow();
        assertTrue(runtime.hasPendingRevoltFor(occupationId));
        runtime.resolve(record.id(), RevoltState.FAILED, 1L);
        assertFalse(runtime.hasPendingRevoltFor(occupationId));
    }
}
