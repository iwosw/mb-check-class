package com.talhanation.bannermod.settlement.prefab.player;

import com.talhanation.bannermod.persistence.SavedDataVersioning;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PlayerBuildingRegistrySavedData extends SavedData {
    private static final String FILE_ID = "bannermodPlayerBuildingRegistry";
    private static final SavedData.Factory<PlayerBuildingRegistrySavedData> FACTORY = new SavedData.Factory<>(PlayerBuildingRegistrySavedData::new, PlayerBuildingRegistrySavedData::load);

    private static final int CURRENT_VERSION = 1;
    private final List<Entry> entries = new ArrayList<>();

    public static PlayerBuildingRegistrySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, FILE_ID);
    }

    public static PlayerBuildingRegistrySavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        SavedDataVersioning.migrate(tag, CURRENT_VERSION, "PlayerBuildingRegistrySavedData");
        PlayerBuildingRegistrySavedData data = new PlayerBuildingRegistrySavedData();
        ListTag list = tag.getList("Entries", Tag.TAG_COMPOUND);
        for (Tag raw : list) {
            if (!(raw instanceof CompoundTag entryTag)) {
                continue;
            }
            data.entries.add(Entry.fromTag(entryTag));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        SavedDataVersioning.putVersion(tag, CURRENT_VERSION);
        ListTag list = new ListTag();
        for (Entry entry : entries) {
            list.add(entry.toTag());
        }
        tag.put("Entries", list);
        return tag;
    }

    public List<Entry> entries() {
        return Collections.unmodifiableList(entries);
    }

    public void add(Entry entry) {
        this.entries.add(entry);
        setDirty();
    }

    public record Entry(
            UUID id,
            UUID owner,
            String prefabId,
            BlockPos min,
            BlockPos max,
            BlockPos center,
            BlockPos keyBlock,
            long createdAt
    ) {
        private CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("Id", id);
            tag.putUUID("Owner", owner);
            tag.putString("PrefabId", prefabId);
            tag.putLong("Min", min.asLong());
            tag.putLong("Max", max.asLong());
            tag.putLong("Center", center.asLong());
            tag.putLong("KeyBlock", keyBlock.asLong());
            tag.putLong("CreatedAt", createdAt);
            return tag;
        }

        private static Entry fromTag(CompoundTag tag) {
            UUID id = tag.hasUUID("Id") ? tag.getUUID("Id") : UUID.randomUUID();
            UUID owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : new UUID(0L, 0L);
            String prefabId = tag.getString("PrefabId");
            BlockPos min = BlockPos.of(tag.getLong("Min"));
            BlockPos max = BlockPos.of(tag.getLong("Max"));
            BlockPos center = BlockPos.of(tag.getLong("Center"));
            BlockPos keyBlock = BlockPos.of(tag.getLong("KeyBlock"));
            long createdAt = tag.getLong("CreatedAt");
            return new Entry(id, owner, prefabId, min, max, center, keyBlock, createdAt);
        }
    }
}
