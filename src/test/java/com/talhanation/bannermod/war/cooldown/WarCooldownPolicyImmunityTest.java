package com.talhanation.bannermod.war.cooldown;

import com.talhanation.bannermod.war.runtime.DemilitarizationRuntime;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarCooldownPolicyImmunityTest {

    private static final UUID ATTACKER = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID DEFENDER = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void canDeclareWithImmunityRejectsWhenDefenderHasImmunityActive() {
        WarCooldownRuntime cooldowns = new WarCooldownRuntime();
        cooldowns.grant(DEFENDER, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 5000L);

        WarCooldownPolicy.Result result = WarCooldownPolicy.canDeclareWithImmunity(
                ATTACKER, DEFENDER,
                List.of(),
                1000L,
                WarCooldownPolicy.DEFAULT_PEACE_COOLDOWN_TICKS,
                WarCooldownPolicy.DEFAULT_DEFENDER_DAILY_DECLARATIONS,
                new DemilitarizationRuntime(),
                cooldowns
        );

        assertFalse(result.valid());
        assertEquals("defender_lost_territory_immunity", result.reason());
    }

    @Test
    void canDeclareWithImmunityAllowsAfterImmunityExpiry() {
        WarCooldownRuntime cooldowns = new WarCooldownRuntime();
        cooldowns.grant(DEFENDER, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 5000L);

        WarCooldownPolicy.Result result = WarCooldownPolicy.canDeclareWithImmunity(
                ATTACKER, DEFENDER,
                List.of(),
                6000L,
                WarCooldownPolicy.DEFAULT_PEACE_COOLDOWN_TICKS,
                WarCooldownPolicy.DEFAULT_DEFENDER_DAILY_DECLARATIONS,
                new DemilitarizationRuntime(),
                cooldowns
        );

        assertTrue(result.valid());
    }

    @Test
    void canTogglePeacefulRejectsWhenCooldownActive() {
        WarCooldownRuntime cooldowns = new WarCooldownRuntime();
        cooldowns.grant(ATTACKER, WarCooldownKind.PEACEFUL_TOGGLE_RECENT, 3000L);

        WarCooldownPolicy.Result result = WarCooldownPolicy.canTogglePeacefulStatus(ATTACKER, 1000L, cooldowns);
        assertFalse(result.valid());
        assertEquals("peaceful_toggle_cooldown_active", result.reason());
    }

    @Test
    void canTogglePeacefulAllowsAfterCooldownExpiry() {
        WarCooldownRuntime cooldowns = new WarCooldownRuntime();
        cooldowns.grant(ATTACKER, WarCooldownKind.PEACEFUL_TOGGLE_RECENT, 3000L);
        WarCooldownPolicy.Result result = WarCooldownPolicy.canTogglePeacefulStatus(ATTACKER, 4000L, cooldowns);
        assertTrue(result.valid());
    }

    @Test
    void canTogglePeacefulAllowsWhenRuntimeAbsent() {
        assertTrue(WarCooldownPolicy.canTogglePeacefulStatus(ATTACKER, 1000L, null).valid());
    }

    @Test
    void canTogglePeacefulRejectsNullEntity() {
        assertFalse(WarCooldownPolicy.canTogglePeacefulStatus(null, 1000L, new WarCooldownRuntime()).valid());
    }
}
