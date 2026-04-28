package com.talhanation.bannermod.governance;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class BannerModGovernorManager extends SavedData {
    private static final String FILE_ID = "bannermodGovernors";
    private static final SavedData.Factory<BannerModGovernorManager> FACTORY = new SavedData.Factory<>(BannerModGovernorManager::new, BannerModGovernorManager::load);

    private final Map<UUID, BannerModGovernorSnapshot> snapshots = new LinkedHashMap<>();

    public static BannerModGovernorManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, FILE_ID);
    }

    public static BannerModGovernorManager load(CompoundTag tag, HolderLookup.Provider registries) {
        BannerModGovernorManager manager = new BannerModGovernorManager();
        if (tag.contains("Snapshots", Tag.TAG_LIST)) {
            ListTag snapshots = tag.getList("Snapshots", Tag.TAG_COMPOUND);
            for (Tag entry : snapshots) {
                BannerModGovernorSnapshot snapshot = BannerModGovernorSnapshot.fromTag((CompoundTag) entry);
                manager.snapshots.put(snapshot.claimUuid(), snapshot);
            }
        }
        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (BannerModGovernorSnapshot snapshot : this.snapshots.values()) {
            list.add(snapshot.toTag());
        }
        tag.put("Snapshots", list);
        return tag;
    }

    @Nullable
    public BannerModGovernorSnapshot getSnapshot(UUID claimUuid) {
        return this.snapshots.get(claimUuid);
    }

    public BannerModGovernorSnapshot getOrCreateSnapshot(UUID claimUuid, BannerModGovernorSnapshot fallback) {
        BannerModGovernorSnapshot snapshot = this.snapshots.get(claimUuid);
        if (snapshot != null) {
            return snapshot;
        }
        this.snapshots.put(claimUuid, fallback);
        this.setDirty();
        return fallback;
    }

    public void putSnapshot(BannerModGovernorSnapshot snapshot) {
        if (snapshot == null) return;
        BannerModGovernorSnapshot previous = this.snapshots.put(snapshot.claimUuid(), snapshot);
        if (!snapshot.equals(previous)) {
            this.setDirty();
        }
    }

    @Nullable
    public BannerModGovernorSnapshot removeSnapshot(UUID claimUuid) {
        BannerModGovernorSnapshot removed = this.snapshots.remove(claimUuid);
        if (removed != null) {
            this.setDirty();
        }
        return removed;
    }

    public Collection<BannerModGovernorSnapshot> getAllSnapshots() {
        return this.snapshots.values();
    }
}
