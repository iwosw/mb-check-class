package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.war.audit.WarAuditLogSavedData;
import com.talhanation.bannermod.war.retention.WarRetentionPolicy;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TESTLONGRUN-001: long-running declare-resolve simulation against the WARRETENTION-001
 * SavedData stack. Drives N=100 declare-resolve cycles directly on the runtimes (the same
 * runtime classes the live SavedData wraps; see {@link com.talhanation.bannermod.war.WarRuntimeContext})
 * and asserts the retention caps + sweeper-style pruning hold.
 *
 * <p>The acceptance check is a plateau, not unbounded growth: after 100 cycles the audit log
 * stays within {@link WarRetentionPolicy#MAX_AUDIT_ENTRIES}, the occupation runtime stays
 * within {@link WarRetentionPolicy#MAX_OCCUPATIONS}, and per-war revolts never exceed
 * {@link WarRetentionPolicy#MAX_REVOLTS_PER_WAR}. The total revolt count is also bounded by
 * the per-war cap times the number of wars currently retained.</p>
 *
 * <p>Mirrors the unit-test style of {@link com.talhanation.bannermod.war.audit.WarAuditLogRetentionTest},
 * {@link OccupationRuntimeRetentionTest}, and {@link RevoltRuntimeRetentionTest}. The live-SavedData
 * coverage of the same caps lives in {@code BannerModWarRetentionGameTests}.</p>
 */
class WarLongRunRetentionTest {

    private static final int CYCLES = 100;
    /** Audit entries appended per declare-resolve cycle. Sized so 100 cycles overflows the cap. */
    private static final int AUDIT_ENTRIES_PER_CYCLE = 60;
    /** Occupations placed per cycle. Sized so 100 cycles overflows {@link WarRetentionPolicy#MAX_OCCUPATIONS}. */
    private static final int OCCUPATIONS_PER_CYCLE = 12;
    /** Revolts scheduled per cycle (well above {@link WarRetentionPolicy#MAX_REVOLTS_PER_WAR}). */
    private static final int REVOLTS_PER_CYCLE = WarRetentionPolicy.MAX_REVOLTS_PER_WAR + 16;

    @Test
    void hundredDeclareResolveCyclesPlateauAtConfiguredCaps() {
        WarDeclarationRuntime declarations = new WarDeclarationRuntime();
        WarAuditLogSavedData audit = new WarAuditLogSavedData();
        OccupationRuntime occupations = new OccupationRuntime();
        RevoltRuntime revolts = new RevoltRuntime();

        // Sanity check input volumes will actually exercise the caps.
        assertTrue((long) CYCLES * AUDIT_ENTRIES_PER_CYCLE > WarRetentionPolicy.MAX_AUDIT_ENTRIES,
                "test would not exercise audit cap; pick larger AUDIT_ENTRIES_PER_CYCLE");
        assertTrue((long) CYCLES * OCCUPATIONS_PER_CYCLE > WarRetentionPolicy.MAX_OCCUPATIONS,
                "test would not exercise occupations cap; pick larger OCCUPATIONS_PER_CYCLE");
        assertTrue(REVOLTS_PER_CYCLE > WarRetentionPolicy.MAX_REVOLTS_PER_WAR,
                "test would not exercise revolts per-war cap");

        long now = 0L;
        long retention = WarRetentionPolicy.resolvedWarRetentionTicks();
        // Step gameTime each cycle so eventually some resolved wars age past the retention
        // window and get pruned by the sweeper-equivalent. With CYCLES=100 and a one-day step
        // per cycle, the first ~70 cycles age out by the end of the simulation.
        long ticksPerCycleStep = WarRetentionPolicy.ticksFromGameDays(1);

        List<UUID> declaredWarIds = new ArrayList<>(CYCLES);

        for (int cycle = 0; cycle < CYCLES; cycle++) {
            UUID attacker = UUID.randomUUID();
            UUID defender = UUID.randomUUID();

            WarDeclarationRecord war = declarations.declareWar(
                    attacker,
                    defender,
                    WarGoalType.WHITE_PEACE,
                    "longrun-cycle-" + cycle,
                    List.of(),
                    List.of(),
                    List.of(),
                    now,
                    0L
            ).orElseThrow();
            UUID warId = war.id();
            declaredWarIds.add(warId);

            // Audit churn for this war.
            for (int i = 0; i < AUDIT_ENTRIES_PER_CYCLE; i++) {
                audit.append(warId, "CYCLE_TICK", "i=" + i, now + i);
            }

            // Occupation churn for this war.
            UUID occupier = UUID.randomUUID();
            UUID occupied = UUID.randomUUID();
            for (int i = 0; i < OCCUPATIONS_PER_CYCLE; i++) {
                occupations.place(warId, occupier, occupied,
                        List.of(new ChunkPos(cycle, i)), now + i);
            }

            // Revolt churn — sized larger than per-war cap so eviction kicks in within one cycle.
            UUID rebel = UUID.randomUUID();
            for (int i = 0; i < REVOLTS_PER_CYCLE; i++) {
                revolts.schedule(warId, UUID.randomUUID(), rebel, occupier, now + i);
            }

            // Resolve the war and advance time.
            assertTrue(declarations.updateState(warId, WarState.RESOLVED));
            now += ticksPerCycleStep;

            // Sweeper-equivalent: prune resolved-war records older than the retention window.
            // Mirrors WarRetentionSweeper.sweep without requiring a ServerLevel.
            Set<UUID> resolvedIds = collectResolvedWarIds(declarations);
            audit.pruneResolved(resolvedIds, now, retention);
            occupations.pruneResolved(resolvedIds, now, retention);
        }

        // ---- Plateau assertions ----

        // 1. Audit log: bounded by the hard cap regardless of cycles.
        int auditSize = audit.size();
        assertTrue(auditSize <= WarRetentionPolicy.MAX_AUDIT_ENTRIES,
                "audit log size " + auditSize + " exceeded MAX_AUDIT_ENTRIES "
                        + WarRetentionPolicy.MAX_AUDIT_ENTRIES + " after " + CYCLES + " cycles");
        // Naive linear growth would be CYCLES * AUDIT_ENTRIES_PER_CYCLE. Confirm we are well below.
        long linearAudit = (long) CYCLES * AUDIT_ENTRIES_PER_CYCLE;
        assertTrue(auditSize < linearAudit,
                "audit log did not plateau; expected < " + linearAudit + " (linear growth), got " + auditSize);

        // 2. Occupations: bounded by the hard cap regardless of cycles.
        int occSize = occupations.all().size();
        assertTrue(occSize <= WarRetentionPolicy.MAX_OCCUPATIONS,
                "occupation count " + occSize + " exceeded MAX_OCCUPATIONS "
                        + WarRetentionPolicy.MAX_OCCUPATIONS + " after " + CYCLES + " cycles");
        long linearOcc = (long) CYCLES * OCCUPATIONS_PER_CYCLE;
        assertTrue(occSize < linearOcc,
                "occupations did not plateau; expected < " + linearOcc + " (linear growth), got " + occSize);

        // 3. Revolts: per-war cap holds for every war, and total is bounded by per-war cap times
        //    the number of distinct wars still represented in the runtime.
        int totalRevolts = revolts.all().size();
        Set<UUID> warsWithRevolts = new HashSet<>();
        for (RevoltRecord r : revolts.all()) {
            if (r.warId() != null) {
                warsWithRevolts.add(r.warId());
            }
        }
        for (UUID warId : warsWithRevolts) {
            long countForWar = revolts.all().stream().filter(r -> warId.equals(r.warId())).count();
            assertTrue(countForWar <= WarRetentionPolicy.MAX_REVOLTS_PER_WAR,
                    "revolts for war " + warId + " = " + countForWar
                            + " exceeded MAX_REVOLTS_PER_WAR " + WarRetentionPolicy.MAX_REVOLTS_PER_WAR);
        }
        long upperBound = (long) WarRetentionPolicy.MAX_REVOLTS_PER_WAR * warsWithRevolts.size();
        assertTrue(totalRevolts <= upperBound,
                "total revolts " + totalRevolts + " exceeded per-war-cap budget " + upperBound);
        long linearRevolts = (long) CYCLES * REVOLTS_PER_CYCLE;
        assertTrue(totalRevolts < linearRevolts,
                "revolts did not plateau; expected < " + linearRevolts + " (linear growth), got " + totalRevolts);

        // 4. Sweeper actually pruned aged-out resolved wars: the audit log should not still hold
        //    entries from the very first cycle (gameTime 0..AUDIT_ENTRIES_PER_CYCLE) because that
        //    war resolved at cycle 0 and is now older than the 30-day retention window.
        UUID firstWarId = declaredWarIds.get(0);
        long stillHasFirstWarAudit = audit.all().stream()
                .filter(e -> firstWarId.equals(e.warId()))
                .count();
        assertEquals(0L, stillHasFirstWarAudit,
                "expected first cycle's audit entries to be pruned by the sweeper-equivalent");
    }

    private static Set<UUID> collectResolvedWarIds(WarDeclarationRuntime declarations) {
        Set<UUID> ids = new HashSet<>();
        for (WarDeclarationRecord record : declarations.all()) {
            if (record.state() == WarState.RESOLVED || record.state() == WarState.CANCELLED) {
                ids.add(record.id());
            }
        }
        return ids;
    }
}
