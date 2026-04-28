package com.talhanation.bannermod.settlement.bootstrap;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SettlementRegistryData extends SavedData {
    private static final String FILE_ID = "bannermodSettlementRegistry";
    private static final SavedData.Factory<SettlementRegistryData> FACTORY = new SavedData.Factory<>(SettlementRegistryData::new, SettlementRegistryData::load);

    private final Map<UUID, SettlementRecord> records = new LinkedHashMap<>();
    private final Map<Long, UUID> settlementByAuthorityChunk = new LinkedHashMap<>();
    private final Map<UUID, UUID> settlementByClaimId = new LinkedHashMap<>();

    public static SettlementRegistryData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, FILE_ID);
    }

    public static SettlementRegistryData load(CompoundTag tag, HolderLookup.Provider registries) {
        SettlementRegistryData data = new SettlementRegistryData();
        ListTag list = tag.getList("Settlements", Tag.TAG_COMPOUND);
        for (Tag raw : list) {
            if (!(raw instanceof CompoundTag entryTag)) {
                continue;
            }
            SettlementRecord record = SettlementRecord.fromTag(entryTag);
            data.records.put(record.settlementId(), record);
        }
        data.rebuildIndexes();
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (SettlementRecord record : this.records.values()) {
            list.add(record.toTag());
        }
        tag.put("Settlements", list);
        return tag;
    }

    public SettlementRecord put(SettlementRecord record) {
        if (record == null) {
            return null;
        }
        SettlementRecord previous = this.records.put(record.settlementId(), record);
        rebuildIndexes();
        setDirty();
        return previous;
    }

    public SettlementRecord get(UUID settlementId) {
        return settlementId == null ? null : this.records.get(settlementId);
    }

    public SettlementRecord getSettlementAt(ChunkPos chunkPos) {
        if (chunkPos == null) {
            return null;
        }
        UUID settlementId = this.settlementByAuthorityChunk.get(chunkPos.toLong());
        return settlementId == null ? null : this.records.get(settlementId);
    }

    public SettlementRecord getSettlementByClaimId(UUID claimId) {
        if (claimId == null) {
            return null;
        }
        UUID settlementId = this.settlementByClaimId.get(claimId);
        return settlementId == null ? null : this.records.get(settlementId);
    }

    public Collection<SettlementRecord> all() {
        return List.copyOf(this.records.values());
    }

    private void rebuildIndexes() {
        this.settlementByAuthorityChunk.clear();
        this.settlementByClaimId.clear();
        for (SettlementRecord record : this.records.values()) {
            ChunkPos authorityChunk = new ChunkPos(record.authorityPos());
            this.settlementByAuthorityChunk.put(authorityChunk.toLong(), record.settlementId());
            this.settlementByClaimId.put(record.claimId(), record.settlementId());
        }
    }
}
