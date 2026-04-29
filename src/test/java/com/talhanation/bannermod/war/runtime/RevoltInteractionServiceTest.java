package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.war.audit.WarAuditLogSavedData;
import com.talhanation.bannermod.war.registry.PoliticalRegistryRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RevoltInteractionServiceTest {
    private static final UUID ATTACKER = UUID.fromString("00000000-0000-0000-0000-000000007001");
    private static final UUID DEFENDER = UUID.fromString("00000000-0000-0000-0000-000000007002");

    @Test
    void operatorCanResolvePendingRevoltSuccessAndRemoveOccupation() {
        TestRuntime runtime = createRuntime();
        RevoltRecord revolt = runtime.revolts.schedule(runtime.occupation.id(), DEFENDER, ATTACKER, 10L).orElseThrow();

        RevoltInteractionService.Result result = RevoltInteractionService.resolve(
                runtime.revolts, runtime.occupations, runtime.applier, revolt.id(), RevoltState.SUCCESS, true, 20L);

        assertTrue(result.allowed());
        assertEquals(RevoltState.SUCCESS, runtime.revolts.byId(revolt.id()).orElseThrow().state());
        assertTrue(runtime.occupations.byId(runtime.occupation.id()).isEmpty());
    }

    @Test
    void nonOperatorGetsDeniedAndStateStaysPending() {
        TestRuntime runtime = createRuntime();
        RevoltRecord revolt = runtime.revolts.schedule(runtime.occupation.id(), DEFENDER, ATTACKER, 10L).orElseThrow();

        RevoltInteractionService.Result result = RevoltInteractionService.resolve(
                runtime.revolts, runtime.occupations, runtime.applier, revolt.id(), RevoltState.FAILED, false, 20L);

        assertFalse(result.allowed());
        assertEquals("op_only", result.reason());
        assertEquals(RevoltState.PENDING, runtime.revolts.byId(revolt.id()).orElseThrow().state());
        assertTrue(runtime.occupations.byId(runtime.occupation.id()).isPresent());
    }

    private static TestRuntime createRuntime() {
        WarDeclarationRuntime declarations = new WarDeclarationRuntime();
        UUID warId = declarations.declareWar(ATTACKER, DEFENDER, WarGoalType.OCCUPATION, "revolt-test",
                List.of(BlockPos.ZERO), List.of(), List.of(), 1L, 0L).orElseThrow().id();
        OccupationRuntime occupations = new OccupationRuntime();
        OccupationRecord occupation = occupations.place(warId, ATTACKER, DEFENDER,
                List.of(new ChunkPos(0, 0)), 2L).orElseThrow();
        WarOutcomeApplier applier = new WarOutcomeApplier(declarations, new SiegeStandardRuntime(),
                new WarAuditLogSavedData(), occupations, new DemilitarizationRuntime(), new PoliticalRegistryRuntime());
        return new TestRuntime(new RevoltRuntime(), occupations, occupation, applier);
    }

    private record TestRuntime(RevoltRuntime revolts,
                               OccupationRuntime occupations,
                               OccupationRecord occupation,
                               WarOutcomeApplier applier) {
    }
}
