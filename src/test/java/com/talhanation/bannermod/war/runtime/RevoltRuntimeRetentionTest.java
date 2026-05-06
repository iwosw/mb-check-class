package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.war.retention.WarRetentionPolicy;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RevoltRuntimeRetentionTest {

    @Test
    void scheduleBeyondPerWarCapEvictsOldestRevolts() {
        RevoltRuntime runtime = new RevoltRuntime();
        UUID warId = UUID.randomUUID();
        UUID rebel = UUID.randomUUID();
        UUID occupier = UUID.randomUUID();
        for (int i = 0; i < 100; i++) {
            UUID occupationId = UUID.randomUUID();
            assertTrue(runtime.schedule(warId, occupationId, rebel, occupier, i).isPresent());
        }
        long countForWar = runtime.all().stream()
                .filter(r -> warId.equals(r.warId()))
                .count();
        assertEquals(WarRetentionPolicy.MAX_REVOLTS_PER_WAR, countForWar);
    }

    @Test
    void perWarCapDoesNotAffectOtherWars() {
        RevoltRuntime runtime = new RevoltRuntime();
        UUID warA = UUID.randomUUID();
        UUID warB = UUID.randomUUID();
        UUID rebel = UUID.randomUUID();
        UUID occupier = UUID.randomUUID();
        for (int i = 0; i < WarRetentionPolicy.MAX_REVOLTS_PER_WAR + 10; i++) {
            runtime.schedule(warA, UUID.randomUUID(), rebel, occupier, i);
        }
        for (int i = 0; i < 5; i++) {
            runtime.schedule(warB, UUID.randomUUID(), rebel, occupier, i);
        }
        long countA = runtime.all().stream().filter(r -> warA.equals(r.warId())).count();
        long countB = runtime.all().stream().filter(r -> warB.equals(r.warId())).count();
        assertEquals(WarRetentionPolicy.MAX_REVOLTS_PER_WAR, countA);
        assertEquals(5L, countB);
    }
}
