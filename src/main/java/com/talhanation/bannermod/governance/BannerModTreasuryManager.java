package com.talhanation.bannermod.governance;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class BannerModTreasuryManager extends SavedData {
    private static final String FILE_ID = "bannermodTreasury";

    private final Map<UUID, BannerModTreasuryLedgerSnapshot> ledgers = new LinkedHashMap<>();

    public static BannerModTreasuryManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(BannerModTreasuryManager::load, BannerModTreasuryManager::new, FILE_ID);
    }

    public static BannerModTreasuryManager load(CompoundTag tag) {
        BannerModTreasuryManager manager = new BannerModTreasuryManager();
        if (tag.contains("Ledgers", Tag.TAG_LIST)) {
            ListTag ledgers = tag.getList("Ledgers", Tag.TAG_COMPOUND);
            for (Tag entry : ledgers) {
                BannerModTreasuryLedgerSnapshot snapshot = BannerModTreasuryLedgerSnapshot.fromTag((CompoundTag) entry);
                manager.ledgers.put(snapshot.claimUuid(), snapshot);
            }
        }
        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (BannerModTreasuryLedgerSnapshot ledger : this.ledgers.values()) {
            list.add(ledger.toTag());
        }
        tag.put("Ledgers", list);
        return tag;
    }

    @Nullable
    public BannerModTreasuryLedgerSnapshot getLedger(UUID claimUuid) {
        return this.ledgers.get(claimUuid);
    }

    public BannerModTreasuryLedgerSnapshot getOrCreateLedger(UUID claimUuid, BannerModTreasuryLedgerSnapshot fallback) {
        return this.ledgers.computeIfAbsent(claimUuid, ignored -> fallback);
    }

    public void putLedger(BannerModTreasuryLedgerSnapshot ledger) {
        if (ledger == null) {
            return;
        }
        this.ledgers.put(ledger.claimUuid(), ledger);
        this.setDirty();
    }

    public BannerModTreasuryLedgerSnapshot depositTaxes(UUID claimUuid,
                                                        ChunkPos anchorChunk,
                                                        @Nullable String settlementFactionId,
                                                        int taxAmount,
                                                        long depositTick) {
        BannerModTreasuryLedgerSnapshot ledger = getOrCreateLedger(
                claimUuid,
                BannerModTreasuryLedgerSnapshot.create(claimUuid, anchorChunk, settlementFactionId)
        );
        BannerModTreasuryLedgerSnapshot updated = ledger
                .withSettlementIdentity(anchorChunk, settlementFactionId)
                .withDeposit(taxAmount, depositTick);
        putLedger(updated);
        return updated;
    }

    public BannerModTreasuryLedgerSnapshot recordArmyUpkeepDebit(UUID claimUuid,
                                                                 ChunkPos anchorChunk,
                                                                 @Nullable String settlementFactionId,
                                                                 int requestedDebit,
                                                                 long debitTick) {
        BannerModTreasuryLedgerSnapshot ledger = getOrCreateLedger(
                claimUuid,
                BannerModTreasuryLedgerSnapshot.create(claimUuid, anchorChunk, settlementFactionId)
        );
        BannerModTreasuryLedgerSnapshot updated = ledger
                .withSettlementIdentity(anchorChunk, settlementFactionId)
                .withArmyUpkeepDebit(requestedDebit, debitTick);
        putLedger(updated);
        return updated;
    }

    public BannerModTreasuryLedgerSnapshot applyHeartbeatAccounting(UUID claimUuid,
                                                                     ChunkPos anchorChunk,
                                                                     @Nullable String settlementFactionId,
                                                                    int taxAmount,
                                                                    int requestedArmyUpkeepDebit,
                                                                    long accountingTick) {
        BannerModTreasuryLedgerSnapshot ledger = this.ledgers.get(claimUuid);
        if (ledger == null && taxAmount <= 0 && requestedArmyUpkeepDebit <= 0) {
            return BannerModTreasuryLedgerSnapshot.create(claimUuid, anchorChunk, settlementFactionId);
        }
        if (ledger == null) {
            ledger = BannerModTreasuryLedgerSnapshot.create(claimUuid, anchorChunk, settlementFactionId);
        }
        BannerModTreasuryLedgerSnapshot updated = ledger
                .withSettlementIdentity(anchorChunk, settlementFactionId)
                .withDeposit(taxAmount, accountingTick)
                .withArmyUpkeepDebit(requestedArmyUpkeepDebit, accountingTick);
        putLedger(updated);
        return updated;
    }

    @Nullable
    public BannerModTreasuryLedgerSnapshot removeLedger(UUID claimUuid) {
        BannerModTreasuryLedgerSnapshot removed = this.ledgers.remove(claimUuid);
        if (removed != null) {
            this.setDirty();
        }
        return removed;
    }

    public Collection<BannerModTreasuryLedgerSnapshot> getAllLedgers() {
        return this.ledgers.values();
    }
}
