package com.talhanation.bannermod.war.cooldown;

import com.talhanation.bannermod.war.runtime.WarDeclarationRecord;
import com.talhanation.bannermod.war.runtime.WarGoalType;
import com.talhanation.bannermod.war.runtime.WarState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarCooldownPolicyTest {

    private static WarDeclarationRecord war(UUID attacker, UUID defender, long declaredAt, WarState state) {
        return new WarDeclarationRecord(
                UUID.randomUUID(),
                attacker,
                defender,
                WarGoalType.WHITE_PEACE,
                "",
                List.of(),
                List.of(),
                List.of(),
                declaredAt,
                declaredAt,
                state
        );
    }

    @Test
    void rejectsSelfDeclaration() {
        UUID a = UUID.randomUUID();
        WarCooldownPolicy.Result result = WarCooldownPolicy.canDeclare(
                a, a, List.of(), 0L, 0L, 1);
        assertFalse(result.valid());
        assertEquals("self_declaration", result.reason());
    }

    @Test
    void rejectsMissingParty() {
        UUID a = UUID.randomUUID();
        WarCooldownPolicy.Result result = WarCooldownPolicy.canDeclare(
                a, null, List.of(), 0L, 0L, 1);
        assertFalse(result.valid());
        assertEquals("missing_party", result.reason());
    }

    @Test
    void blocksWhenPriorWarStillWithinPeaceCooldown() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        long now = 100_000L;
        long cooldown = 50_000L;
        WarDeclarationRecord prior = war(a, b, now - 10_000L, WarState.RESOLVED);
        WarCooldownPolicy.Result result = WarCooldownPolicy.canDeclare(
                a, b, List.of(prior), now, cooldown, 1);
        assertFalse(result.valid());
        assertEquals("peace_cooldown_active", result.reason());
    }

    @Test
    void allowsAfterCooldownExpires() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        long now = 100_000L;
        long cooldown = 50_000L;
        WarDeclarationRecord prior = war(a, b, now - 60_000L, WarState.RESOLVED);
        WarCooldownPolicy.Result result = WarCooldownPolicy.canDeclare(
                a, b, List.of(prior), now, cooldown, 1);
        assertTrue(result.valid(), "should clear cooldown when prior war is older than the cooldown window");
    }

    @Test
    void blocksDefenderDailyLimit() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        long now = 1_000_000L;
        WarDeclarationRecord existing = war(c, b, now - 100L, WarState.DECLARED);
        WarCooldownPolicy.Result result = WarCooldownPolicy.canDeclare(
                a, b, List.of(existing), now, 0L, 1);
        assertFalse(result.valid());
        assertEquals("defender_daily_limit", result.reason());
    }

    @Test
    void cancelledDeclarationsDoNotCountTowardsDailyLimit() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        long now = 1_000_000L;
        WarDeclarationRecord cancelled = war(c, b, now - 100L, WarState.CANCELLED);
        WarCooldownPolicy.Result result = WarCooldownPolicy.canDeclare(
                a, b, List.of(cancelled), now, 0L, 1);
        assertTrue(result.valid());
    }
}
