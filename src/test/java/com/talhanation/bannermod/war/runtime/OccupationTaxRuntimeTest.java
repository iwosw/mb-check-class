package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.governance.BannerModTreasuryManager;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsClaimManager;
import com.talhanation.bannermod.war.audit.WarAuditEntry;
import com.talhanation.bannermod.war.audit.WarAuditLogSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OccupationTaxRuntimeTest {

    private static final long TICK_PER_DAY = 24L * 60L * 60L * 20L;

    private static RecruitsClaim claim(String name, UUID owner, ChunkPos pos) {
        RecruitsClaim claim = new RecruitsClaim(name, owner);
        claim.setCenter(pos);
        claim.addChunk(pos);
        return claim;
    }

    private static OccupationRecord seed(OccupationRuntime runtime, UUID warId,
                                         UUID occupier, UUID occupied,
                                         List<ChunkPos> chunks, long startedAt) {
        return runtime.place(warId, occupier, occupied, chunks, startedAt).orElseThrow();
    }

    // ------------------------------------------------------------------------
    // OccupationRecord NBT round-trip + legacy fallback
    // ------------------------------------------------------------------------

    @Test
    void recordRoundTripPreservesLastTaxedAtGameTime() {
        OccupationRecord rec = new OccupationRecord(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                List.of(new ChunkPos(0, 0), new ChunkPos(1, 1)),
                100L, 250L);
        OccupationRecord restored = OccupationRecord.fromTag(rec.toTag());
        assertEquals(rec.id(), restored.id());
        assertEquals(100L, restored.startedAtGameTime());
        assertEquals(250L, restored.lastTaxedAtGameTime());
    }

    @Test
    void recordLegacyTagFallsBackToStartedAt() {
        // Old saves had no LastTaxedAtGameTime — ensure fromTag falls back to startedAt.
        OccupationRecord rec = new OccupationRecord(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                List.of(new ChunkPos(0, 0)),
                42L);
        CompoundTag tag = rec.toTag();
        tag.remove("LastTaxedAtGameTime");
        OccupationRecord restored = OccupationRecord.fromTag(tag);
        assertEquals(42L, restored.lastTaxedAtGameTime());
        assertEquals(42L, restored.startedAtGameTime());
    }

    @Test
    void runtimeUpdateLastTaxedAtMutatesAndFiresDirty() {
        OccupationRuntime runtime = new OccupationRuntime();
        boolean[] dirty = { false };
        runtime.setDirtyListener(() -> dirty[0] = true);
        OccupationRecord rec = seed(runtime, UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), List.of(new ChunkPos(0, 0)), 0L);
        dirty[0] = false;

        runtime.updateLastTaxedAt(rec.id(), 500L);
        assertTrue(dirty[0]);
        assertEquals(500L, runtime.byId(rec.id()).orElseThrow().lastTaxedAtGameTime());

        dirty[0] = false;
        runtime.updateLastTaxedAt(rec.id(), 500L); // identical — no-op, no dirty
        assertFalse(dirty[0]);
    }

    // ------------------------------------------------------------------------
    // OccupationTaxPolicy — pure due-selection + idempotency
    // ------------------------------------------------------------------------

    @Test
    void policySelectsOnlyDueOccupations() {
        OccupationRuntime runtime = new OccupationRuntime();
        OccupationRecord earlier = seed(runtime, UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), List.of(new ChunkPos(0, 0)), 0L);
        OccupationRecord later = seed(runtime, UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), List.of(new ChunkPos(1, 1)), 500L);

        // currentTick=600, intervalTicks=400 → earlier (last=0, elapsed=600) due,
        // later (last=500, elapsed=100) not due.
        List<OccupationTaxPolicy.DueOccupation> due =
                OccupationTaxPolicy.selectDue(runtime.all(), 600L, 400L);
        assertEquals(1, due.size());
        assertEquals(earlier.id(), due.get(0).record().id());
        assertEquals(400L, due.get(0).advanceTo());
    }

    @Test
    void policyAdvancesByOneIntervalNotToCurrentTick() {
        OccupationRuntime runtime = new OccupationRuntime();
        OccupationRecord rec = seed(runtime, UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), List.of(new ChunkPos(0, 0)), 0L);
        // currentTick=10000, interval=400. Without per-call cap this would advance to 10000;
        // policy must instead advance to 0 + 400 = 400, so backpay catches up gradually.
        List<OccupationTaxPolicy.DueOccupation> due =
                OccupationTaxPolicy.selectDue(runtime.all(), 10_000L, 400L);
        assertEquals(1, due.size());
        assertEquals(400L, due.get(0).advanceTo());
        assertEquals(rec.id(), due.get(0).record().id());
    }

    @Test
    void policyEmptyOnZeroOrNegativeInterval() {
        OccupationRuntime runtime = new OccupationRuntime();
        seed(runtime, UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), List.of(new ChunkPos(0, 0)), 0L);
        assertTrue(OccupationTaxPolicy.selectDue(runtime.all(), 1000L, 0L).isEmpty());
        assertTrue(OccupationTaxPolicy.selectDue(runtime.all(), 1000L, -50L).isEmpty());
    }

    @Test
    void taxOwedSaturatesAndZeroForBadInputs() {
        assertEquals(0, OccupationTaxPolicy.taxOwed(0, 5));
        assertEquals(0, OccupationTaxPolicy.taxOwed(5, 0));
        assertEquals(0, OccupationTaxPolicy.taxOwed(-1, 5));
        assertEquals(15, OccupationTaxPolicy.taxOwed(3, 5));
        assertEquals(Integer.MAX_VALUE,
                OccupationTaxPolicy.taxOwed(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    // ------------------------------------------------------------------------
    // OccupationTaxRuntime — treasury transfer, audit, idempotency
    // ------------------------------------------------------------------------

    @Test
    void accrueTransfersFundsAndAuditsPaid() {
        UUID occupier = UUID.randomUUID();
        UUID occupied = UUID.randomUUID();
        UUID warId = UUID.randomUUID();

        OccupationRuntime occupations = new OccupationRuntime();
        OccupationRecord rec = seed(occupations, warId, occupier, occupied,
                List.of(new ChunkPos(0, 0), new ChunkPos(1, 0)), 0L);

        BannerModTreasuryManager treasury = new BannerModTreasuryManager();
        RecruitsClaimManager claims = new RecruitsClaimManager();
        RecruitsClaim defenderClaim = claim("D", occupied, new ChunkPos(0, 0));
        RecruitsClaim attackerClaim = claim("A", occupier, new ChunkPos(50, 50));
        claims.testInsertClaim(defenderClaim);
        claims.testInsertClaim(attackerClaim);
        treasury.depositTaxes(defenderClaim.getUUID(), defenderClaim.getCenter(), null, 100, 0L);

        WarAuditLogSavedData audit = new WarAuditLogSavedData();
        OccupationTaxRuntime runtime = new OccupationTaxRuntime(occupations, treasury, claims, audit);

        // 2 chunks * 5 per chunk = 10
        runtime.accrue(5, 400L, 600L);

        assertEquals(90, treasury.getLedger(defenderClaim.getUUID()).treasuryBalance());
        assertEquals(10, treasury.getLedger(attackerClaim.getUUID()).treasuryBalance());
        assertEquals(400L, occupations.byId(rec.id()).orElseThrow().lastTaxedAtGameTime());
        assertEquals(1, audit.all().size());
        WarAuditEntry entry = audit.all().get(0);
        assertEquals("OCCUPATION_TAX_PAID", entry.type());
        assertEquals(warId, entry.warId());
        assertTrue(entry.detail().contains("requested=10"));
        assertTrue(entry.detail().contains("paid=10"));
        assertTrue(entry.detail().contains("chunks=2"));
        assertTrue(entry.detail().contains(rec.id().toString()));
    }

    @Test
    void accrueAuditsDefaultedAmountWhenDefenderShortOfFunds() {
        UUID occupier = UUID.randomUUID();
        UUID occupied = UUID.randomUUID();
        UUID warId = UUID.randomUUID();

        OccupationRuntime occupations = new OccupationRuntime();
        OccupationRecord rec = seed(occupations, warId, occupier, occupied,
                List.of(new ChunkPos(0, 0), new ChunkPos(1, 0), new ChunkPos(2, 0)), 0L);

        BannerModTreasuryManager treasury = new BannerModTreasuryManager();
        RecruitsClaimManager claims = new RecruitsClaimManager();
        RecruitsClaim defenderClaim = claim("D", occupied, new ChunkPos(0, 0));
        RecruitsClaim attackerClaim = claim("A", occupier, new ChunkPos(50, 50));
        claims.testInsertClaim(defenderClaim);
        claims.testInsertClaim(attackerClaim);
        treasury.depositTaxes(defenderClaim.getUUID(), defenderClaim.getCenter(), null, 7, 0L);

        WarAuditLogSavedData audit = new WarAuditLogSavedData();
        OccupationTaxRuntime runtime = new OccupationTaxRuntime(occupations, treasury, claims, audit);

        // 3 chunks * 5 = 15 requested; only 7 available → paid=7, defaulted=8.
        runtime.accrue(5, 400L, 600L);

        assertEquals(0, treasury.getLedger(defenderClaim.getUUID()).treasuryBalance());
        assertEquals(7, treasury.getLedger(attackerClaim.getUUID()).treasuryBalance());
        assertEquals(2, audit.all().size());
        WarAuditEntry paid = audit.all().get(0);
        WarAuditEntry defaulted = audit.all().get(1);
        assertEquals("OCCUPATION_TAX_PAID", paid.type());
        assertTrue(paid.detail().contains("paid=7"));
        assertEquals("OCCUPATION_TAX_DEFAULTED", defaulted.type());
        assertTrue(defaulted.detail().contains("requested=15"));
        assertTrue(defaulted.detail().contains("defaulted=8"));
        assertTrue(defaulted.detail().contains("paid=7"));
        // Timestamp still advanced — defaulted amount is recorded, not silently carried.
        assertEquals(400L, occupations.byId(rec.id()).orElseThrow().lastTaxedAtGameTime());
    }

    @Test
    void accrueAuditsOnlyDefaultedWhenAttackerHasNoLedgerAndNothingMoves() {
        // No attacker claim → winnerLedgerClaim is null, so transfer returns 0 with no debit.
        UUID occupier = UUID.randomUUID();
        UUID occupied = UUID.randomUUID();
        UUID warId = UUID.randomUUID();

        OccupationRuntime occupations = new OccupationRuntime();
        OccupationRecord rec = seed(occupations, warId, occupier, occupied,
                List.of(new ChunkPos(0, 0)), 0L);

        BannerModTreasuryManager treasury = new BannerModTreasuryManager();
        RecruitsClaimManager claims = new RecruitsClaimManager();
        RecruitsClaim defenderClaim = claim("D", occupied, new ChunkPos(0, 0));
        claims.testInsertClaim(defenderClaim);
        treasury.depositTaxes(defenderClaim.getUUID(), defenderClaim.getCenter(), null, 100, 0L);

        WarAuditLogSavedData audit = new WarAuditLogSavedData();
        OccupationTaxRuntime runtime = new OccupationTaxRuntime(occupations, treasury, claims, audit);

        runtime.accrue(5, 400L, 600L);

        assertEquals(100, treasury.getLedger(defenderClaim.getUUID()).treasuryBalance(),
                "no attacker ledger: defender must NOT be debited");
        assertEquals(1, audit.all().size());
        WarAuditEntry only = audit.all().get(0);
        assertEquals("OCCUPATION_TAX_DEFAULTED", only.type());
        assertTrue(only.detail().contains("paid=0"));
        assertTrue(only.detail().contains("defaulted=5"));
        assertEquals(400L, occupations.byId(rec.id()).orElseThrow().lastTaxedAtGameTime());
    }

    @Test
    void accrueIsIdempotentWithinInterval() {
        UUID occupier = UUID.randomUUID();
        UUID occupied = UUID.randomUUID();
        UUID warId = UUID.randomUUID();

        OccupationRuntime occupations = new OccupationRuntime();
        OccupationRecord rec = seed(occupations, warId, occupier, occupied,
                List.of(new ChunkPos(0, 0)), 0L);

        BannerModTreasuryManager treasury = new BannerModTreasuryManager();
        RecruitsClaimManager claims = new RecruitsClaimManager();
        RecruitsClaim defenderClaim = claim("D", occupied, new ChunkPos(0, 0));
        RecruitsClaim attackerClaim = claim("A", occupier, new ChunkPos(50, 50));
        claims.testInsertClaim(defenderClaim);
        claims.testInsertClaim(attackerClaim);
        treasury.depositTaxes(defenderClaim.getUUID(), defenderClaim.getCenter(), null, 1000, 0L);

        WarAuditLogSavedData audit = new WarAuditLogSavedData();
        OccupationTaxRuntime runtime = new OccupationTaxRuntime(occupations, treasury, claims, audit);

        // First call charges one cycle (paid=5); subsequent calls inside the same window are no-ops.
        runtime.accrue(5, 400L, 600L);
        runtime.accrue(5, 400L, 700L);
        runtime.accrue(5, 400L, 750L);

        assertEquals(995, treasury.getLedger(defenderClaim.getUUID()).treasuryBalance());
        assertEquals(5, treasury.getLedger(attackerClaim.getUUID()).treasuryBalance());
        assertEquals(1, audit.all().size());
        assertEquals(400L, occupations.byId(rec.id()).orElseThrow().lastTaxedAtGameTime());

        // After a second interval elapses, a second cycle accrues.
        runtime.accrue(5, 400L, 1200L);
        assertEquals(990, treasury.getLedger(defenderClaim.getUUID()).treasuryBalance());
        assertEquals(10, treasury.getLedger(attackerClaim.getUUID()).treasuryBalance());
        assertEquals(2, audit.all().size());
        assertEquals(800L, occupations.byId(rec.id()).orElseThrow().lastTaxedAtGameTime());
    }

    @Test
    void accrueLongPauseChargesAtMostOneCyclePerCall() {
        UUID occupier = UUID.randomUUID();
        UUID occupied = UUID.randomUUID();
        UUID warId = UUID.randomUUID();

        OccupationRuntime occupations = new OccupationRuntime();
        OccupationRecord rec = seed(occupations, warId, occupier, occupied,
                List.of(new ChunkPos(0, 0)), 0L);

        BannerModTreasuryManager treasury = new BannerModTreasuryManager();
        RecruitsClaimManager claims = new RecruitsClaimManager();
        RecruitsClaim defenderClaim = claim("D", occupied, new ChunkPos(0, 0));
        RecruitsClaim attackerClaim = claim("A", occupier, new ChunkPos(50, 50));
        claims.testInsertClaim(defenderClaim);
        claims.testInsertClaim(attackerClaim);
        treasury.depositTaxes(defenderClaim.getUUID(), defenderClaim.getCenter(), null, 1000, 0L);

        WarAuditLogSavedData audit = new WarAuditLogSavedData();
        OccupationTaxRuntime runtime = new OccupationTaxRuntime(occupations, treasury, claims, audit);

        // Simulate a 5-day pause with intervalTicks=1day. Single accrue call should charge
        // exactly one cycle and advance lastTaxedAt by exactly one interval.
        runtime.accrue(10, TICK_PER_DAY, 5L * TICK_PER_DAY);
        assertEquals(990, treasury.getLedger(defenderClaim.getUUID()).treasuryBalance());
        assertEquals(10, treasury.getLedger(attackerClaim.getUUID()).treasuryBalance());
        assertEquals(TICK_PER_DAY, occupations.byId(rec.id()).orElseThrow().lastTaxedAtGameTime());
    }

    @Test
    void accrueWithZeroTaxOrIntervalIsNoop() {
        UUID occupier = UUID.randomUUID();
        UUID occupied = UUID.randomUUID();

        OccupationRuntime occupations = new OccupationRuntime();
        OccupationRecord rec = seed(occupations, UUID.randomUUID(), occupier, occupied,
                List.of(new ChunkPos(0, 0)), 0L);

        BannerModTreasuryManager treasury = new BannerModTreasuryManager();
        RecruitsClaimManager claims = new RecruitsClaimManager();
        RecruitsClaim defenderClaim = claim("D", occupied, new ChunkPos(0, 0));
        claims.testInsertClaim(defenderClaim);
        treasury.depositTaxes(defenderClaim.getUUID(), defenderClaim.getCenter(), null, 100, 0L);

        WarAuditLogSavedData audit = new WarAuditLogSavedData();
        OccupationTaxRuntime runtime = new OccupationTaxRuntime(occupations, treasury, claims, audit);

        runtime.accrue(0, 400L, 1000L);
        runtime.accrue(5, 0L, 1000L);

        assertEquals(100, treasury.getLedger(defenderClaim.getUUID()).treasuryBalance());
        assertTrue(audit.all().isEmpty());
        assertEquals(0L, occupations.byId(rec.id()).orElseThrow().lastTaxedAtGameTime());
        assertNotNull(rec); // record still present
    }
}
