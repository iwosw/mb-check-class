package com.talhanation.bannermod.war.runtime;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DemilitarizationRuntimeTest {

    @Test
    void imposeStoresRecord() {
        DemilitarizationRuntime runtime = new DemilitarizationRuntime();
        UUID entity = UUID.randomUUID();
        DemilitarizationRecord record = runtime.impose(entity, UUID.randomUUID(), 100L).orElseThrow();
        assertEquals(entity, record.politicalEntityId());
        assertEquals(100L, record.endsAtGameTime());
    }

    @Test
    void isDemilitarizedReturnsTrueWhileActive() {
        DemilitarizationRuntime runtime = new DemilitarizationRuntime();
        UUID entity = UUID.randomUUID();
        runtime.impose(entity, UUID.randomUUID(), 100L);
        assertTrue(runtime.isDemilitarized(entity, 50L));
        assertFalse(runtime.isDemilitarized(entity, 200L));
    }

    @Test
    void pruneExpiredRemovesOldRecords() {
        DemilitarizationRuntime runtime = new DemilitarizationRuntime();
        runtime.impose(UUID.randomUUID(), UUID.randomUUID(), 100L);
        runtime.impose(UUID.randomUUID(), UUID.randomUUID(), 500L);
        int pruned = runtime.pruneExpired(150L);
        assertEquals(1, pruned);
        assertEquals(1, runtime.all().size());
    }
}
