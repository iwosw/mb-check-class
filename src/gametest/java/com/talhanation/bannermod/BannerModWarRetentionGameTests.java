package com.talhanation.bannermod;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.audit.WarAuditLogSavedData;
import com.talhanation.bannermod.war.events.WarRetentionSweeper;
import com.talhanation.bannermod.war.retention.WarRetentionPolicy;
import com.talhanation.bannermod.war.runtime.OccupationRecord;
import com.talhanation.bannermod.war.runtime.OccupationRuntime;
import com.talhanation.bannermod.war.runtime.RevoltRuntime;
import com.talhanation.bannermod.war.runtime.WarDeclarationRecord;
import com.talhanation.bannermod.war.runtime.WarDeclarationRuntime;
import com.talhanation.bannermod.war.runtime.WarGoalType;
import com.talhanation.bannermod.war.runtime.WarState;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/**
 * End-to-end retention guards for WARRETENTION-001 against the live SavedData stack.
 *
 * <p>These tests bypass the political-registry setup the other war gametests use and
 * exercise the SavedData containers directly through {@link WarRuntimeContext}. The goal
 * is to assert the retention policy holds against the same SavedData instances the server
 * persists at world-save — not to re-test domain wiring covered by
 * {@code BannerModWarOutcomeAndTaxGameTests}.</p>
 */
@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModWarRetentionGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void auditLogCapsAtMaxEntriesUnderLiveSavedData(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        WarAuditLogSavedData audit = WarRuntimeContext.audit(level);
        int sizeBefore = audit.size();

        UUID warId = UUID.randomUUID();
        for (int i = 0; i < 5000; i++) {
            audit.append(warId, "RET_TEST", "i=" + i, level.getGameTime() + i);
        }

        helper.assertTrue(audit.size() <= WarRetentionPolicy.MAX_AUDIT_ENTRIES,
                "Expected audit log size <= MAX_AUDIT_ENTRIES (" + WarRetentionPolicy.MAX_AUDIT_ENTRIES
                        + ") after 5000 appends, got " + audit.size() + " (sizeBefore=" + sizeBefore + ")");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void occupationRuntimeCapsAtMaxOccupationsUnderLiveSavedData(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        OccupationRuntime occupations = WarRuntimeContext.occupations(level);
        int sizeBefore = occupations.all().size();

        UUID warId = UUID.randomUUID();
        for (int i = 0; i < 2000; i++) {
            occupations.place(warId, UUID.randomUUID(), UUID.randomUUID(),
                    List.of(new ChunkPos(i, i)), level.getGameTime() + i);
        }

        helper.assertTrue(occupations.all().size() <= WarRetentionPolicy.MAX_OCCUPATIONS,
                "Expected occupations size <= MAX_OCCUPATIONS (" + WarRetentionPolicy.MAX_OCCUPATIONS
                        + ") after 2000 places, got " + occupations.all().size()
                        + " (sizeBefore=" + sizeBefore + ")");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void revoltRuntimeCapsPerWarUnderLiveSavedData(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        RevoltRuntime revolts = WarRuntimeContext.revolts(level);

        UUID warId = UUID.randomUUID();
        UUID rebel = UUID.randomUUID();
        UUID occupier = UUID.randomUUID();
        for (int i = 0; i < 100; i++) {
            revolts.schedule(warId, UUID.randomUUID(), rebel, occupier, level.getGameTime() + i);
        }

        long countForWar = revolts.all().stream().filter(r -> warId.equals(r.warId())).count();
        helper.assertTrue(countForWar <= WarRetentionPolicy.MAX_REVOLTS_PER_WAR,
                "Expected revolts for one war <= MAX_REVOLTS_PER_WAR ("
                        + WarRetentionPolicy.MAX_REVOLTS_PER_WAR + "), got " + countForWar);
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void prunesResolvedWarOlderThanRetentionWindowOnLiveSavedData(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        WarAuditLogSavedData audit = WarRuntimeContext.audit(level);
        OccupationRuntime occupations = WarRuntimeContext.occupations(level);
        WarDeclarationRuntime declarations = WarRuntimeContext.declarations(level);

        // Use an explicit synthetic "now" that is well past the retention window so the
        // cutoff arithmetic does not depend on the gametest server's actual gameTime
        // (which can be near zero on a fresh harness world).
        long retention = WarRetentionPolicy.resolvedWarRetentionTicks();
        long farPast = 0L;
        long syntheticNow = retention + 100_000L;

        WarDeclarationRecord declared = declarations.declareWar(
                UUID.randomUUID(),
                UUID.randomUUID(),
                WarGoalType.WHITE_PEACE,
                "retention",
                List.of(),
                List.of(),
                List.of(),
                farPast,
                0L
        ).orElseThrow();
        declarations.updateState(declared.id(), WarState.RESOLVED);
        UUID warId = declared.id();

        audit.append(warId, "OLD_RESOLVED", "x", farPast);
        OccupationRecord occOld = occupations.place(warId, UUID.randomUUID(), UUID.randomUUID(),
                List.of(new ChunkPos(99, 99)), farPast).orElseThrow();

        int auditRemoved = audit.pruneResolved(List.of(warId), syntheticNow, retention);
        int occRemoved = occupations.pruneResolved(List.of(warId), syntheticNow, retention);
        int totalRemoved = auditRemoved + occRemoved;

        helper.assertTrue(totalRemoved >= 2,
                "Expected pruneResolved to remove at least 2 records for the resolved war, got " + totalRemoved
                        + " (audit=" + auditRemoved + ", occ=" + occRemoved + ")");
        helper.assertTrue(occupations.byId(occOld.id()).isEmpty(),
                "Expected old occupation for resolved war to be pruned");
        boolean stillHasOldAudit = audit.all().stream()
                .anyMatch(e -> warId.equals(e.warId()) && "OLD_RESOLVED".equals(e.type()));
        helper.assertTrue(!stillHasOldAudit,
                "Expected old audit entry for resolved war to be pruned");

        // Smoke-test the registered sweeper too — it should be a no-op against this newly
        // synthetic-now scenario but must not blow up against a populated SavedData stack.
        int sweepResult = WarRetentionSweeper.sweep(level);
        helper.assertTrue(sweepResult >= 0,
                "Expected WarRetentionSweeper.sweep to return non-negative, got " + sweepResult);
        helper.succeed();
    }
}
