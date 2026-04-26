package com.talhanation.bannermod.war.cooldown;

import com.talhanation.bannermod.war.runtime.DemilitarizationRuntime;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarCooldownDemilitarizationTest {

    @Test
    void demilitarizedAttackerIsBlocked() {
        DemilitarizationRuntime demilitarizations = new DemilitarizationRuntime();
        UUID attacker = UUID.randomUUID();
        UUID defender = UUID.randomUUID();
        demilitarizations.impose(attacker, UUID.randomUUID(), 1_000_000L);

        WarCooldownPolicy.Result result = WarCooldownPolicy.canDeclare(
                attacker, defender, List.of(), 100L, 0L, 1, demilitarizations);
        assertFalse(result.valid());
        assertEquals("attacker_demilitarized", result.reason());
    }

    @Test
    void expiredDemilitarizationDoesNotBlock() {
        DemilitarizationRuntime demilitarizations = new DemilitarizationRuntime();
        UUID attacker = UUID.randomUUID();
        UUID defender = UUID.randomUUID();
        demilitarizations.impose(attacker, UUID.randomUUID(), 50L);

        WarCooldownPolicy.Result result = WarCooldownPolicy.canDeclare(
                attacker, defender, List.of(), 100L, 0L, 1, demilitarizations);
        assertTrue(result.valid());
    }

    @Test
    void nullDemilitarizationRuntimeIsOk() {
        WarCooldownPolicy.Result result = WarCooldownPolicy.canDeclare(
                UUID.randomUUID(), UUID.randomUUID(), List.of(), 0L, 0L, 1, null);
        assertTrue(result.valid());
    }
}
