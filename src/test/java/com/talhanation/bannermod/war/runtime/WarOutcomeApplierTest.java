package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.war.audit.WarAuditEntry;
import com.talhanation.bannermod.war.audit.WarAuditLogSavedData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarOutcomeApplierTest {

    private static UUID seed(WarDeclarationRuntime runtime, UUID attacker, UUID defender, WarState state) {
        WarDeclarationRecord record = runtime.declareWar(
                attacker, defender, WarGoalType.WHITE_PEACE, "",
                List.of(), List.of(), List.of(), 0L, 0L
        ).orElseThrow();
        if (state != WarState.DECLARED) {
            runtime.updateState(record.id(), state);
        }
        return record.id();
    }

    @Test
    void whitePeaceResolvesAndAudits() {
        WarDeclarationRuntime runtime = new WarDeclarationRuntime();
        UUID warId = seed(runtime, UUID.randomUUID(), UUID.randomUUID(), WarState.DECLARED);
        WarAuditLogSavedData audit = new WarAuditLogSavedData();
        WarOutcomeApplier applier = new WarOutcomeApplier(runtime, new SiegeStandardRuntime(), audit);

        WarOutcomeApplier.Result result = applier.applyWhitePeace(warId, 100L);
        assertTrue(result.valid());
        assertEquals(WarOutcomeType.WHITE_PEACE, result.outcome());
        assertEquals(WarState.RESOLVED, runtime.byId(warId).orElseThrow().state());
        assertEquals(1, audit.all().size());
        WarAuditEntry entry = audit.all().get(0);
        assertEquals(warId, entry.warId());
        assertEquals("OUTCOME_APPLIED", entry.type());
    }

    @Test
    void tributeRejectsNegativeAmount() {
        WarDeclarationRuntime runtime = new WarDeclarationRuntime();
        UUID warId = seed(runtime, UUID.randomUUID(), UUID.randomUUID(), WarState.DECLARED);
        WarOutcomeApplier applier = new WarOutcomeApplier(runtime, new SiegeStandardRuntime(), new WarAuditLogSavedData());

        WarOutcomeApplier.Result result = applier.applyTribute(warId, -5L, 0L);
        assertFalse(result.valid());
        assertEquals("negative_tribute", result.reason());
    }

    @Test
    void cancelOnlyAllowedFromDeclared() {
        WarDeclarationRuntime runtime = new WarDeclarationRuntime();
        UUID warId = seed(runtime, UUID.randomUUID(), UUID.randomUUID(), WarState.ACTIVE);
        WarOutcomeApplier applier = new WarOutcomeApplier(runtime, new SiegeStandardRuntime(), new WarAuditLogSavedData());

        WarOutcomeApplier.Result result = applier.cancel(warId, 0L, "test");
        assertFalse(result.valid());
        assertEquals("not_cancellable", result.reason());
    }

    @Test
    void unknownWarReturnsInvalid() {
        WarDeclarationRuntime runtime = new WarDeclarationRuntime();
        WarOutcomeApplier applier = new WarOutcomeApplier(runtime, new SiegeStandardRuntime(), new WarAuditLogSavedData());
        WarOutcomeApplier.Result result = applier.applyWhitePeace(UUID.randomUUID(), 0L);
        assertFalse(result.valid());
        assertEquals("unknown_war", result.reason());
        assertNotNull(result);
    }
}
