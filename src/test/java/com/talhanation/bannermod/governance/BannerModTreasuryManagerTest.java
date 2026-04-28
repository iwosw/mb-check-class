package com.talhanation.bannermod.governance;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerModTreasuryManagerTest {

    @Test
    void managerRoundTripsClaimKeyedTreasuryLedger() {
        UUID claimUuid = UUID.randomUUID();
        BannerModTreasuryManager manager = new BannerModTreasuryManager();

        manager.depositTaxes(claimUuid, new ChunkPos(4, 7), "blueguild", 12, 100L);
        manager.depositTaxes(claimUuid, new ChunkPos(4, 7), "blueguild", 8, 140L);

        CompoundTag persisted = manager.save(new CompoundTag(), null);
        BannerModTreasuryManager restored = BannerModTreasuryManager.load(persisted, null);
        BannerModTreasuryLedgerSnapshot ledger = restored.getLedger(claimUuid);

        assertNotNull(ledger);
        assertEquals(claimUuid, ledger.claimUuid());
        assertEquals(new ChunkPos(4, 7), ledger.anchorChunk());
        assertEquals("blueguild", ledger.settlementFactionId());
        assertEquals(20, ledger.accruedTaxes());
        assertEquals(0, ledger.spentArmyUpkeep());
        assertEquals(20, ledger.treasuryBalance());
        assertEquals(8, ledger.lastDepositAmount());
        assertEquals(140L, ledger.lastDepositTick());
        assertEquals(0, ledger.lastArmyUpkeepDebitAmount());
        assertEquals(0L, ledger.lastArmyUpkeepDebitTick());
        assertNull(restored.getLedger(UUID.randomUUID()));
    }

    @Test
    void zeroDepositKeepsExistingAccrualWhileRefreshingSettlementIdentity() {
        UUID claimUuid = UUID.randomUUID();
        BannerModTreasuryManager manager = new BannerModTreasuryManager();

        manager.depositTaxes(claimUuid, new ChunkPos(1, 1), "blueguild", 10, 50L);
        BannerModTreasuryLedgerSnapshot updated = manager.depositTaxes(claimUuid, new ChunkPos(3, 4), "greenguild", 0, 90L);

        assertEquals(new ChunkPos(3, 4), updated.anchorChunk());
        assertEquals("greenguild", updated.settlementFactionId());
        assertEquals(10, updated.accruedTaxes());
        assertEquals(0, updated.lastDepositAmount());
        assertEquals(50L, updated.lastDepositTick());
    }

    @Test
    void armyUpkeepDebitIsBoundedByAvailableTreasuryBalance() {
        UUID claimUuid = UUID.randomUUID();
        BannerModTreasuryManager manager = new BannerModTreasuryManager();

        manager.depositTaxes(claimUuid, new ChunkPos(1, 1), "blueguild", 5, 50L);
        BannerModTreasuryLedgerSnapshot firstDebit = manager.recordArmyUpkeepDebit(claimUuid, new ChunkPos(1, 1), "blueguild", 3, 60L);
        BannerModTreasuryLedgerSnapshot secondDebit = manager.recordArmyUpkeepDebit(claimUuid, new ChunkPos(1, 1), "blueguild", 4, 70L);

        assertEquals(5, secondDebit.accruedTaxes());
        assertEquals(5, secondDebit.spentArmyUpkeep());
        assertEquals(0, secondDebit.treasuryBalance());
        assertEquals(2, secondDebit.lastArmyUpkeepDebitAmount());
        assertEquals(70L, secondDebit.lastArmyUpkeepDebitTick());
        assertEquals(3, firstDebit.lastArmyUpkeepDebitAmount());
    }

    @Test
    void heartbeatAccountingProducesBoundedFiscalProjection() {
        UUID claimUuid = UUID.randomUUID();
        BannerModTreasuryManager manager = new BannerModTreasuryManager();

        manager.depositTaxes(claimUuid, new ChunkPos(1, 1), "blueguild", 4, 40L);
        BannerModTreasuryLedgerSnapshot updated = manager.applyHeartbeatAccounting(claimUuid, new ChunkPos(1, 1), "blueguild", 6, 8, 80L);
        BannerModTreasuryLedgerSnapshot.FiscalRollup rollup = updated.projectFiscalRollup(6, 8, 120L);

        assertEquals(10, updated.accruedTaxes());
        assertEquals(8, updated.spentArmyUpkeep());
        assertEquals(2, updated.treasuryBalance());
        assertEquals(-2, updated.fiscalRollup().lastNetChange());
        assertEquals(0, rollup.projectedNextBalance());
        assertEquals(120L, rollup.accountingTick());
        assertTrue(rollup.projectedNextBalance() >= 0);
    }

    @Test
    void noOpHeartbeatAccountingDoesNotPersistEmptyLedger() {
        UUID claimUuid = UUID.randomUUID();
        BannerModTreasuryManager manager = new BannerModTreasuryManager();

        BannerModTreasuryLedgerSnapshot updated = manager.applyHeartbeatAccounting(claimUuid, new ChunkPos(2, 2), "blueguild", 0, 0, 100L);

        assertEquals(0, updated.treasuryBalance());
        assertNull(manager.getLedger(claimUuid));
    }

    @Test
    void getOrCreateLedgerMarksSavedDataDirtyWhenLedgerIsInserted() {
        UUID claimUuid = UUID.randomUUID();
        BannerModTreasuryManager manager = new BannerModTreasuryManager();

        manager.getOrCreateLedger(claimUuid, BannerModTreasuryLedgerSnapshot.create(claimUuid, new ChunkPos(2, 2), "blueguild"));

        assertTrue(manager.isDirty());
    }

    @Test
    void putLedgerDoesNotMarkSavedDataDirtyWhenLedgerIsUnchanged() {
        UUID claimUuid = UUID.randomUUID();
        BannerModTreasuryLedgerSnapshot ledger = BannerModTreasuryLedgerSnapshot.create(claimUuid, new ChunkPos(2, 2), "blueguild");
        BannerModTreasuryManager manager = new BannerModTreasuryManager();
        manager.putLedger(ledger);
        BannerModTreasuryManager reloaded = BannerModTreasuryManager.load(manager.save(new CompoundTag(), null), null);

        reloaded.putLedger(ledger);

        assertFalse(reloaded.isDirty());
    }
}
