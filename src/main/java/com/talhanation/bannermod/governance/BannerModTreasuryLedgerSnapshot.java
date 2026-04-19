package com.talhanation.bannermod.governance;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nullable;
import java.util.UUID;

public record BannerModTreasuryLedgerSnapshot(
        UUID claimUuid,
        int anchorChunkX,
        int anchorChunkZ,
        @Nullable String settlementFactionId,
        int accruedTaxes,
        int spentArmyUpkeep,
        int lastDepositAmount,
        long lastDepositTick,
        int lastArmyUpkeepDebitAmount,
        long lastArmyUpkeepDebitTick
) {
    public record FiscalRollup(
            int accruedTaxes,
            int spentArmyUpkeep,
            int treasuryBalance,
            int lastNetChange,
            int projectedNextBalance,
            long accountingTick
    ) {
    }

    public ChunkPos anchorChunk() {
        return new ChunkPos(this.anchorChunkX, this.anchorChunkZ);
    }

    public BannerModTreasuryLedgerSnapshot withSettlementIdentity(ChunkPos anchorChunk, @Nullable String settlementFactionId) {
        return new BannerModTreasuryLedgerSnapshot(
                this.claimUuid,
                anchorChunk.x,
                anchorChunk.z,
                normalizeFactionId(settlementFactionId),
                this.accruedTaxes,
                this.spentArmyUpkeep,
                this.lastDepositAmount,
                this.lastDepositTick,
                this.lastArmyUpkeepDebitAmount,
                this.lastArmyUpkeepDebitTick
        );
    }

    public BannerModTreasuryLedgerSnapshot withDeposit(int depositAmount, long depositTick) {
        int normalizedDeposit = Math.max(0, depositAmount);
        return new BannerModTreasuryLedgerSnapshot(
                this.claimUuid,
                this.anchorChunkX,
                this.anchorChunkZ,
                this.settlementFactionId,
                this.accruedTaxes + normalizedDeposit,
                this.spentArmyUpkeep,
                normalizedDeposit,
                normalizedDeposit > 0 ? depositTick : this.lastDepositTick,
                this.lastArmyUpkeepDebitAmount,
                this.lastArmyUpkeepDebitTick
        );
    }

    public int treasuryBalance() {
        return Math.max(0, this.accruedTaxes - this.spentArmyUpkeep);
    }

    public BannerModTreasuryLedgerSnapshot withArmyUpkeepDebit(int requestedDebit, long debitTick) {
        int appliedDebit = Math.min(Math.max(0, requestedDebit), treasuryBalance());
        return new BannerModTreasuryLedgerSnapshot(
                this.claimUuid,
                this.anchorChunkX,
                this.anchorChunkZ,
                this.settlementFactionId,
                this.accruedTaxes,
                this.spentArmyUpkeep + appliedDebit,
                this.lastDepositAmount,
                this.lastDepositTick,
                appliedDebit,
                appliedDebit > 0 ? debitTick : this.lastArmyUpkeepDebitTick
        );
    }

    public FiscalRollup fiscalRollup() {
        long accountingTick = Math.max(this.lastDepositTick, this.lastArmyUpkeepDebitTick);
        return new FiscalRollup(
                this.accruedTaxes,
                this.spentArmyUpkeep,
                treasuryBalance(),
                this.lastDepositAmount - this.lastArmyUpkeepDebitAmount,
                treasuryBalance(),
                accountingTick
        );
    }

    public FiscalRollup projectFiscalRollup(int projectedDepositAmount, int requestedArmyUpkeepDebit, long accountingTick) {
        int normalizedDeposit = Math.max(0, projectedDepositAmount);
        int balanceAfterDeposit = treasuryBalance() + normalizedDeposit;
        int appliedProjectedDebit = Math.min(Math.max(0, requestedArmyUpkeepDebit), balanceAfterDeposit);
        return new FiscalRollup(
                this.accruedTaxes,
                this.spentArmyUpkeep,
                treasuryBalance(),
                this.lastDepositAmount - this.lastArmyUpkeepDebitAmount,
                Math.max(0, balanceAfterDeposit - appliedProjectedDebit),
                accountingTick
        );
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("ClaimUuid", this.claimUuid);
        tag.putInt("AnchorChunkX", this.anchorChunkX);
        tag.putInt("AnchorChunkZ", this.anchorChunkZ);
        if (this.settlementFactionId != null && !this.settlementFactionId.isBlank()) {
            tag.putString("SettlementFactionId", this.settlementFactionId);
        }
        tag.putInt("AccruedTaxes", this.accruedTaxes);
        tag.putInt("SpentArmyUpkeep", this.spentArmyUpkeep);
        tag.putInt("LastDepositAmount", this.lastDepositAmount);
        tag.putLong("LastDepositTick", this.lastDepositTick);
        tag.putInt("LastArmyUpkeepDebitAmount", this.lastArmyUpkeepDebitAmount);
        tag.putLong("LastArmyUpkeepDebitTick", this.lastArmyUpkeepDebitTick);
        return tag;
    }

    public static BannerModTreasuryLedgerSnapshot create(UUID claimUuid, ChunkPos anchorChunk, @Nullable String settlementFactionId) {
        return new BannerModTreasuryLedgerSnapshot(
                claimUuid,
                anchorChunk.x,
                anchorChunk.z,
                normalizeFactionId(settlementFactionId),
                0,
                0,
                0,
                0L,
                0,
                0L
        );
    }

    public static BannerModTreasuryLedgerSnapshot fromTag(CompoundTag tag) {
        return new BannerModTreasuryLedgerSnapshot(
                tag.getUUID("ClaimUuid"),
                tag.getInt("AnchorChunkX"),
                tag.getInt("AnchorChunkZ"),
                tag.contains("SettlementFactionId", Tag.TAG_STRING) ? tag.getString("SettlementFactionId") : null,
                tag.getInt("AccruedTaxes"),
                tag.contains("SpentArmyUpkeep", Tag.TAG_INT) ? tag.getInt("SpentArmyUpkeep") : 0,
                tag.getInt("LastDepositAmount"),
                tag.getLong("LastDepositTick"),
                tag.contains("LastArmyUpkeepDebitAmount", Tag.TAG_INT) ? tag.getInt("LastArmyUpkeepDebitAmount") : 0,
                tag.contains("LastArmyUpkeepDebitTick", Tag.TAG_LONG) ? tag.getLong("LastArmyUpkeepDebitTick") : 0L
        );
    }

    @Nullable
    private static String normalizeFactionId(@Nullable String factionId) {
        if (factionId == null) {
            return null;
        }
        String normalized = factionId.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
