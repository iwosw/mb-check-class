package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.governance.BannerModTreasuryLedgerSnapshot;
import com.talhanation.bannermod.governance.BannerModTreasuryManager;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsClaimManager;
import com.talhanation.bannermod.war.audit.WarAuditLogSavedData;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Side-effecting tax engine for active {@link OccupationRecord}s.
 *
 * <p>Each {@link #accrue(int, long, long)} call walks every occupation, asks
 * {@link OccupationTaxPolicy#selectDue} which ones owe a cycle, debits the defender's
 * claim treasuries up to the requested amount, deposits the actually-paid total to the
 * occupier's first claim ledger, and records {@code OCCUPATION_TAX_PAID} /
 * {@code OCCUPATION_TAX_DEFAULTED} audit entries. The occupation's
 * {@code lastTaxedAtGameTime} is advanced by exactly one interval per cycle so repeat
 * calls within the same interval are no-ops, and a missed window (server downtime)
 * catches up gradually instead of draining the defender in one burst.</p>
 */
public final class OccupationTaxRuntime {
    private final OccupationRuntime occupations;
    @Nullable
    private final BannerModTreasuryManager treasury;
    @Nullable
    private final RecruitsClaimManager claimManager;
    private final WarAuditLogSavedData audit;

    public OccupationTaxRuntime(OccupationRuntime occupations,
                                @Nullable BannerModTreasuryManager treasury,
                                @Nullable RecruitsClaimManager claimManager,
                                WarAuditLogSavedData audit) {
        this.occupations = occupations;
        this.treasury = treasury;
        this.claimManager = claimManager;
        this.audit = audit;
    }

    public void accrue(int taxPerChunk, long intervalTicks, long currentTick) {
        if (taxPerChunk <= 0 || intervalTicks <= 0L) {
            return;
        }
        for (OccupationTaxPolicy.DueOccupation due :
                OccupationTaxPolicy.selectDue(occupations.all(), currentTick, intervalTicks)) {
            processOne(due, taxPerChunk, currentTick);
        }
    }

    private void processOne(OccupationTaxPolicy.DueOccupation due,
                            int taxPerChunk, long currentTick) {
        OccupationRecord record = due.record();
        int requested = OccupationTaxPolicy.taxOwed(record.chunks().size(), taxPerChunk);
        if (requested <= 0) {
            occupations.updateLastTaxedAt(record.id(), due.advanceTo());
            return;
        }
        int paid = transferOccupationTax(record, requested, currentTick);
        int defaulted = requested - paid;

        // Always advance the timestamp so the next interval is metered from this cycle —
        // unpaid amounts are recorded as OCCUPATION_TAX_DEFAULTED, never carried as silent debt.
        occupations.updateLastTaxedAt(record.id(), due.advanceTo());

        if (paid > 0) {
            audit.append(record.warId(), "OCCUPATION_TAX_PAID",
                    "occupationId=" + record.id()
                            + ";chunks=" + record.chunks().size()
                            + ";requested=" + requested
                            + ";paid=" + paid,
                    currentTick);
        }
        if (defaulted > 0) {
            audit.append(record.warId(), "OCCUPATION_TAX_DEFAULTED",
                    "occupationId=" + record.id()
                            + ";chunks=" + record.chunks().size()
                            + ";requested=" + requested
                            + ";paid=" + paid
                            + ";defaulted=" + defaulted,
                    currentTick);
        }
    }

    private int transferOccupationTax(OccupationRecord record, int requested, long currentTick) {
        UUID occupiedEntityId = record.occupiedEntityId();
        UUID occupierEntityId = record.occupierEntityId();
        if (treasury == null || claimManager == null
                || requested <= 0 || occupiedEntityId == null || occupierEntityId == null) {
            return 0;
        }
        UUID winnerLedgerClaim = null;
        ChunkPos winnerLedgerAnchor = null;
        for (RecruitsClaim claim : claimManager.getAllClaims()) {
            if (occupierEntityId.equals(claim.getOwnerPoliticalEntityId())) {
                winnerLedgerClaim = claim.getUUID();
                winnerLedgerAnchor = claim.getCenter();
                break;
            }
        }
        if (winnerLedgerClaim == null) {
            return 0;
        }
        int remaining = requested;
        int transferred = 0;
        java.util.Set<UUID> debitedClaimIds = new java.util.HashSet<>();
        for (ChunkPos chunk : record.chunks()) {
            if (remaining <= 0) break;
            RecruitsClaim claim = claimManager.getClaim(chunk);
            if (claim == null || claim.getUUID() == null || !debitedClaimIds.add(claim.getUUID())) continue;
            if (!occupiedEntityId.equals(claim.getOwnerPoliticalEntityId())) continue;
            BannerModTreasuryLedgerSnapshot ledger = treasury.getLedger(claim.getUUID());
            if (ledger == null) continue;
            int balance = ledger.treasuryBalance();
            if (balance <= 0) continue;
            int debit = Math.min(balance, remaining);
            treasury.recordArmyUpkeepDebit(claim.getUUID(), claim.getCenter(), null, debit, currentTick);
            remaining -= debit;
            transferred += debit;
        }
        if (transferred > 0) {
            treasury.depositTaxes(winnerLedgerClaim, winnerLedgerAnchor, null, transferred, currentTick);
        }
        return transferred;
    }
}
